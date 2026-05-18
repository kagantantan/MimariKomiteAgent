package com.mimari.komite.orchestrator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QuestionClassifier {

    public enum QuestionCategory {
        DIAGRAM_ANALYSIS,
        LEGACY_MIGRATION,
        DDD_DESIGN,
        MICROSERVICE_DESIGN,
        DATA_ARCHITECTURE,
        CODE_QUALITY,
        DISTRIBUTED_SYSTEMS,
        GENERAL
    }

    private final ChatClient chatClient;

    public QuestionClassifier(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public QuestionCategory classify(String question) {
        String systemPrompt = """
            Classify the following software architecture question into exactly one category.
            Return ONLY the category name, nothing else.
            
            Categories:
            - DIAGRAM_ANALYSIS: question contains a sequence diagram or asks to analyze one
            - LEGACY_MIGRATION: about migrating PL/SQL, stored procedures, or legacy systems
            - DDD_DESIGN: about domain-driven design, bounded contexts, aggregates
            - MICROSERVICE_DESIGN: about microservice boundaries, service design
            - DATA_ARCHITECTURE: about databases, consistency, transactions
            - CODE_QUALITY: about refactoring, testing, clean code, TDD
            - DISTRIBUTED_SYSTEMS: about distributed systems, messaging, Kafka, events
            - GENERAL: anything else""";

        try {
            String result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(question)
                    .call()
                    .content();

            if (result != null) {
                return QuestionCategory.valueOf(result.trim().toUpperCase());
            }
        } catch (Exception e) {
            // Fallback to GENERAL if classification fails or LLM returns invalid category
        }
        
        return QuestionCategory.GENERAL;
    }
}
