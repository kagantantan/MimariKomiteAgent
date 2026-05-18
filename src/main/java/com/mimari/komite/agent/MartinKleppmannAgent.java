package com.mimari.komite.agent;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import com.mimari.komite.knowledge.ChromaSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MartinKleppmannAgent implements ExpertAgent {

    private final ChatClient chatClient;
    private final ChromaSearchService chromaSearchService;

    @Value("classpath:prompts/martin-kleppmann.txt")
    private Resource promptResource;

    private String systemPrompt;

    public MartinKleppmannAgent(ChatClient.Builder builder, ChromaSearchService chromaSearchService) {
        this.chatClient = builder.build();
        this.chromaSearchService = chromaSearchService;
    }

    @PostConstruct
    public void init() {
        try (Reader reader = new InputStreamReader(promptResource.getInputStream(), StandardCharsets.UTF_8)) {
            this.systemPrompt = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Could not load prompt for Martin Kleppmann", e);
        }
    }

    @Override
    public String getName() {
        return "Martin Kleppmann (Distributed Data)";
    }

    @Override
    public String consult(String question) {
        // 1. Kitaptan ilgili bölümleri bul
        List<String> relevantDocs = chromaSearchService.search("martin-kleppmann", question, 4);

        // 2. Bulunan bölümleri birleştir
        String context = String.join("\n\n---\n\n", relevantDocs);

        // 3. Claude'a sistem promptu + kitap bölümleri + soru gönder
        String userMessage = context.isEmpty()
            ? question
            : "Kitabımdan ilgili bölümler:\n\n" + context + "\n\n---\n\nSoru: " + question;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
