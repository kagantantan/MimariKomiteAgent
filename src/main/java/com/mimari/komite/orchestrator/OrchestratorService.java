package com.mimari.komite.orchestrator;

import com.mimari.komite.agent.ExpertAgent;
import com.mimari.komite.model.ConsultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final List<ExpertAgent> expertAgents;
    private final ChatClient.Builder chatClientBuilder;
    private final QuestionClassifier questionClassifier;

    private static final Map<QuestionClassifier.QuestionCategory, List<String>> CATEGORY_EXPERTS = Map.of(
        QuestionClassifier.QuestionCategory.DIAGRAM_ANALYSIS, List.of(
            "Eric Evans (DDD)", "Vaughn Vernon (DDD)", "Sam Newman (Microservices)",
            "Chris Richardson (Microservices Patterns)", "Martin Kleppmann (Distributed Data)",
            "Martin Fowler (Refactoring & Patterns)", "Pramod Sadalage (Data Architecture)",
            "Richards & Ford (Architecture Hard Parts)", "Nick Tune (Strategic DDD)",
            "Alberto Brandolini (Event Storming)", "Mathias Verraes (Event-Driven)",
            "Vlad Khononov (Modern DDD)", "Uncle Bob (Clean Architecture)",
            "Kent Beck (TDD)", "Michael Feathers (Legacy Code)"
        ),
        QuestionClassifier.QuestionCategory.LEGACY_MIGRATION, List.of(
            "Michael Feathers (Legacy Code)", "Pramod Sadalage (Data Architecture)",
            "Sam Newman (Microservices)", "Martin Fowler (Refactoring & Patterns)",
            "Kent Beck (TDD)", "Chris Richardson (Microservices Patterns)",
            "Uncle Bob (Clean Architecture)"
        ),
        QuestionClassifier.QuestionCategory.DDD_DESIGN, List.of(
            "Eric Evans (DDD)", "Vaughn Vernon (DDD)", "Vlad Khononov (Modern DDD)",
            "Alberto Brandolini (Event Storming)", "Mathias Verraes (Event-Driven)",
            "Nick Tune (Strategic DDD)", "Martin Fowler (Refactoring & Patterns)"
        ),
        QuestionClassifier.QuestionCategory.MICROSERVICE_DESIGN, List.of(
            "Sam Newman (Microservices)", "Chris Richardson (Microservices Patterns)",
            "Richards & Ford (Architecture Hard Parts)", "Martin Fowler (Refactoring & Patterns)",
            "Nick Tune (Strategic DDD)", "Martin Kleppmann (Distributed Data)"
        ),
        QuestionClassifier.QuestionCategory.DATA_ARCHITECTURE, List.of(
            "Martin Kleppmann (Distributed Data)", "Pramod Sadalage (Data Architecture)",
            "Chris Richardson (Microservices Patterns)", "Richards & Ford (Architecture Hard Parts)"
        ),
        QuestionClassifier.QuestionCategory.CODE_QUALITY, List.of(
            "Uncle Bob (Clean Architecture)", "Kent Beck (TDD)",
            "Michael Feathers (Legacy Code)", "Martin Fowler (Refactoring & Patterns)"
        ),
        QuestionClassifier.QuestionCategory.DISTRIBUTED_SYSTEMS, List.of(
            "Martin Kleppmann (Distributed Data)", "Chris Richardson (Microservices Patterns)",
            "Mathias Verraes (Event-Driven)", "Richards & Ford (Architecture Hard Parts)",
            "Sam Newman (Microservices)"
        ),
        QuestionClassifier.QuestionCategory.GENERAL, List.of(
            "Eric Evans (DDD)", "Martin Fowler (Refactoring & Patterns)",
            "Sam Newman (Microservices)", "Chris Richardson (Microservices Patterns)",
            "Richards & Ford (Architecture Hard Parts)", "Uncle Bob (Clean Architecture)",
            "Pramod Sadalage (Data Architecture)"
        )
    );

    public ConsultResponse orchestrateConsultation(String question) {
        // 1. Soruyu kategorize et
        QuestionClassifier.QuestionCategory category = questionClassifier.classify(question);

        // 2. Bu kategori için ilgili uzmanları seç
        List<String> relevantExpertNames = CATEGORY_EXPERTS.getOrDefault(
            category, CATEGORY_EXPERTS.get(QuestionClassifier.QuestionCategory.GENERAL)
        );

        List<ExpertAgent> selectedAgents = expertAgents.stream()
            .filter(agent -> relevantExpertNames.contains(agent.getName()))
            .collect(Collectors.toList());

        // 3. Seçili uzmanları paralel çağır
        Map<String, String> answers = new ConcurrentHashMap<>();
        selectedAgents.stream().forEach(agent -> {
            String answer = agent.consult(question);
            answers.put(agent.getName(), answer);
        });

        // 4. Öncelik sırasına göre sırala
        LinkedHashMap<String, String> orderedAnswers = answers.entrySet().stream()
            .sorted(Comparator.comparingInt(e ->
                relevantExpertNames.indexOf(e.getKey())
            ))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // 5. Sentez üret
        String synthesis = synthesize(question, category, orderedAnswers);

        return ConsultResponse.builder()
                .question(question)
                .expertAnswers(orderedAnswers)
                .synthesis(synthesis)
                .build();
    }

    private String synthesize(String question,
                               QuestionClassifier.QuestionCategory category,
                               LinkedHashMap<String, String> answers) {
        String expertSummary = answers.entrySet().stream()
                .map(e -> "**" + e.getKey() + "**:\n" + e.getValue())
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClientBuilder.build()
                .prompt()
                .system("""
                    You are a synthesis engine for an architectural advisory committee.
                    Synthesize the opinions of multiple world-class experts into a concise summary.
                    
                    Structure your response as:
                    1. **Consensus** - What all experts agree on
                    2. **Key Disagreements** - Where experts have different views
                    3. **Recommended Action** - The most pragmatic next step
                    4. **Primary Expert** - Which expert's advice is most relevant here
                    
                    Be concise. Maximum 400 words.
                    Respond in the same language as the question.
                    """)
                .user("Category: " + category.name() + "\nQuestion: " + question +
                      "\n\nExpert opinions:\n\n" + expertSummary)
                .call()
                .content();
    }
}
