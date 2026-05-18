package com.mimari.komite.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/diagram")
public class DiagramController {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public DiagramController(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder
                .defaultOptions(AnthropicChatOptions.builder()
                        .maxTokens(3000)
                        .build())
                .build();
        this.objectMapper = objectMapper;
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        return raw.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
    }

    private MimeType detectMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.contains("jpeg")) return MimeTypeUtils.IMAGE_JPEG;
        if (contentType != null && contentType.contains("png")) return MimeTypeUtils.IMAGE_PNG;
        if (contentType != null && contentType.contains("gif")) return MimeTypeUtils.IMAGE_GIF;
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".jpg")) return MimeTypeUtils.IMAGE_JPEG;
        if (filename != null && filename.toLowerCase().endsWith(".jpeg")) return MimeTypeUtils.IMAGE_JPEG;
        return MimeTypeUtils.IMAGE_JPEG;
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        MimeType mimeType = detectMimeType(file);
        Media media = new Media(mimeType, new ByteArrayResource(imageBytes));

        String systemPrompt = "Sen bir yazilim mimarisi uzmanisIn. Yüklenen mimari diyagrami analiz et. " +
                "Türkçe yanit ver. SADECE gecerli JSON döndür, markdown KULLANMA. " +
                "Maksimum 5 component ve 3 issue listele: " +
                "{\"analysis\": \"kisa ozet\", \"components\": [\"comp1\", \"comp2\"], \"issues\": [\"issue1\"]}";

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user(u -> u.text("Bu mimari diyagrami analiz et.").media(media))
                .call()
                .content();

        return objectMapper.readValue(cleanJson(raw), AnalyzeResponse.class);
    }

    @PostMapping("/validate")
    public ValidateResponse validate(@RequestBody ValidateRequest request) throws IOException {
        String systemPrompt = "Sen bir yazilim mimarisi uzmanisIn. Türkçe yanit ver. " +
                "SADECE gecerli JSON döndür: {\"validated\": true, \"summary\": \"kisa ozet\", \"questions\": [\"soru1\", \"soru2\"]}";

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("Analiz:\n" + request.analysis() + "\n\nKullanici Cevabi:\n" + request.userAnswer())
                .call()
                .content();

        return objectMapper.readValue(cleanJson(raw), ValidateResponse.class);
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@RequestBody GenerateRequest request) throws IOException {
        String systemPrompt = "Sen bir yazilim mimarisi uzmanisIn. To-Be mimari onerisi uret. Türkçe yanit ver. " +
                "SADECE gecerli JSON döndür. plantUml alanina Mermaid.js sequenceDiagram formatinda yaz, @startuml KULLANMA: {\"toBe\": \"ozet\", \"plantUml\": \"sequenceDiagram\\n    participant A\\n    A->>B: ornek\", \"recommendations\": [\"oneri1\", \"oneri2\"]}";

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("As-Is Mimari:\n" + request.asIs())
                .call()
                .content();

        return objectMapper.readValue(cleanJson(raw), GenerateResponse.class);
    }

    public record AnalyzeResponse(String analysis, List<String> components, List<String> issues) {}
    public record ValidateRequest(String analysis, String userAnswer) {}
    public record ValidateResponse(boolean validated, String summary, List<String> questions) {}
    public record GenerateRequest(String asIs, boolean confirmed) {}
    public record GenerateResponse(String toBe, String plantUml, List<String> recommendations) {}
}
