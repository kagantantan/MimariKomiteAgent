package com.mimari.komite.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimari.komite.model.ConsultResponse;
import com.mimari.komite.orchestrator.OrchestratorService;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagram")
public class DiagramController {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final OrchestratorService orchestratorService;

    public DiagramController(ChatClient.Builder builder, ObjectMapper objectMapper,
                             OrchestratorService orchestratorService) {
        this.chatClient = builder
                .defaultOptions(AnthropicChatOptions.builder()
                        .maxTokens(3000)
                        .build())
                .build();
        this.objectMapper = objectMapper;
        this.orchestratorService = orchestratorService;
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        return raw.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
    }

    private MimeType detectMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.contains("jpeg")) return MimeTypeUtils.IMAGE_JPEG;
        if (contentType != null && contentType.contains("png")) return MimeTypeUtils.IMAGE_PNG;
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".jpg")) return MimeTypeUtils.IMAGE_JPEG;
        if (filename != null && filename.toLowerCase().endsWith(".jpeg")) return MimeTypeUtils.IMAGE_JPEG;
        return MimeTypeUtils.IMAGE_JPEG;
    }

    private byte[] resizeIfNeeded(byte[] imageBytes, MimeType mimeType) throws IOException {
        if (imageBytes.length <= 4 * 1024 * 1024) return imageBytes;
        BufferedImage original = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        if (original == null) return imageBytes;
        double scale = Math.sqrt((4.0 * 1024 * 1024) / imageBytes.length);
        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String format = mimeType.equals(MimeTypeUtils.IMAGE_PNG) ? "png" : "jpg";
        ImageIO.write(resized, format, out);
        return out.toByteArray();
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestParam("file") MultipartFile file) throws IOException {
        MimeType mimeType = detectMimeType(file);
        byte[] imageBytes = resizeIfNeeded(file.getBytes(), mimeType);
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
        // 15 uzmanın görüşünü al
        String consultQuestion = "Aşağıdaki mimari analizi için en kritik 2-3 soruyu sor. " +
                "Bu soruları kullanıcıya yönelt ki As-Is mimariyi daha iyi anlayalım:\n\n" + request.analysis();
        
        com.mimari.komite.model.ConsultResponse expertOpinions = 
                orchestratorService.orchestrateConsultation(consultQuestion);

        // Sentezden sorular çıkar
        String systemPrompt = "Sen bir yazilim mimarisi uzmanisIn. Türkçe yanit ver. " +
                "Asagidaki uzman goruslerinden en kritik 3 soruyu cikar. " +
                "SADECE gecerli JSON döndür: {\"validated\": true, \"summary\": \"kisa ozet\", \"questions\": [\"soru1\", \"soru2\", \"soru3\"]}";

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user("Uzman Gorusleri:\n" + expertOpinions.getSynthesis())
                .call()
                .content();

        return objectMapper.readValue(cleanJson(raw), ValidateResponse.class);
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@RequestBody GenerateRequest request) throws IOException {

        // 1. 15 uzmanın görüşünü al
        String consultQuestion = "Aşağıdaki As-Is mimari analizi için modernizasyon önerilerin nelerdir?\n\n" + request.asIs();
        ConsultResponse expertOpinions = orchestratorService.orchestrateConsultation(consultQuestion);

        // 2. Uzman sentezini kullanarak To-Be üret
        String systemPrompt = "Sen bir kıdemli yazılım mimarısın. " +
                "Sana As-Is mimari analizi ve 15 dünya çapında uzmanın görüşleri verildi. " +
                "Bu görüşleri sentezleyerek domain'e özgü bir To-Be mimarisi üret. " +
                "Genel 'Service1, Service2' gibi isimler KULLANMA, domain'deki gerçek bileşen isimlerini kullan. " +
                "Türkçe yanit ver. " +
                "SADECE gecerli JSON döndür. plantUml alanina Mermaid.js sequenceDiagram formatinda yaz, @startuml KULLANMA: " +
                "{\"toBe\": \"ozet\", \"plantUml\": \"sequenceDiagram\\n    participant A\\n    A->>B: ornek\", \"recommendations\": [\"oneri1\", \"oneri2\"]}";

        String userPrompt = "As-Is Mimari:\n" + request.asIs() +
                "\n\n15 Uzman Sentezi:\n" + expertOpinions.getSynthesis();

        String raw = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        GenerateResponse response = objectMapper.readValue(cleanJson(raw), GenerateResponse.class);

        // 3. Uzman görüşlerini de döndür
        return new GenerateResponse(
                response.toBe(),
                response.plantUml(),
                response.recommendations(),
                expertOpinions.getExpertAnswers()
        );
    }

    public record AnalyzeResponse(String analysis, List<String> components, List<String> issues) {}
    public record ValidateRequest(String analysis, String userAnswer) {}
    public record ValidateResponse(boolean validated, String summary, List<String> questions) {}
    public record GenerateRequest(String asIs, boolean confirmed) {}
    public record GenerateResponse(String toBe, String plantUml, List<String> recommendations,
                                   Map<String, String> expertOpinions) {}
}
