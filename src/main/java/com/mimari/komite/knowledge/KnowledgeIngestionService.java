package com.mimari.komite.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class KnowledgeIngestionService {

    private final EmbeddingModel embeddingModel;
    private final RestClient restClient;

    public KnowledgeIngestionService(EmbeddingModel embeddingModel, RestClient.Builder restClientBuilder) {
        this.embeddingModel = embeddingModel;
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        this.restClient = restClientBuilder
                .requestFactory(factory)
                .baseUrl("http://localhost:8000")
                .build();
    }

    public int ingestPdf(String expert, String filePath, String sourceName) {
        // 1 & 2. Read and split PDF
        PagePdfDocumentReader reader = new PagePdfDocumentReader("file:" + filePath);
        List<Document> pages = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(pages);

        // 3. Get or create collection ID
        String collectionId = getOrCreateCollection(expert);

        // Prepare and send batches to ChromaDB
        int batchSize = 100;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<Document> batch = chunks.subList(i, end);

            List<String> ids = new ArrayList<>();
            List<float[]> embeddings = new ArrayList<>();
            List<String> documents = new ArrayList<>();
            List<Map<String, Object>> metadatas = new ArrayList<>();

            for (Document chunk : batch) {
                ids.add(UUID.randomUUID().toString());
                float[] embedding = embeddingModel.embed(chunk.getText());
                embeddings.add(embedding);
                documents.add(chunk.getText());
                
                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("expert", expert);
                metadata.put("source", sourceName);
                metadatas.add(metadata);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("ids", ids);
            payload.put("embeddings", embeddings);
            payload.put("documents", documents);
            payload.put("metadatas", metadatas);

            try {
                String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
                restClient.post()
                        .uri("/api/v1/collections/{collectionId}/upsert", collectionId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(jsonPayload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to upsert batch for expert: " + expert, ex);
            }
        }

        // 6. Return total chunk count
        return chunks.size();
    }


    public int ingestText(String expert, String text, String sourceName) {
        String collectionId = getOrCreateCollection(expert);

        org.springframework.ai.document.Document doc = new org.springframework.ai.document.Document(text);
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter();
        java.util.List<org.springframework.ai.document.Document> chunks = splitter.apply(java.util.List.of(doc));

        int batchSize = 100;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            java.util.List<org.springframework.ai.document.Document> batch = chunks.subList(i, end);

            java.util.List<String> ids = new java.util.ArrayList<>();
            java.util.List<float[]> embeddings = new java.util.ArrayList<>();
            java.util.List<String> documents = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> metadatas = new java.util.ArrayList<>();

            for (org.springframework.ai.document.Document chunk : batch) {
                ids.add(UUID.randomUUID().toString());
                embeddings.add(embeddingModel.embed(chunk.getText()));
                documents.add(chunk.getText());
                java.util.Map<String, Object> metadata = new java.util.HashMap<>();
                metadata.put("expert", expert);
                metadata.put("source", sourceName);
                metadatas.add(metadata);
            }

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("ids", ids);
            payload.put("embeddings", embeddings);
            payload.put("documents", documents);
            payload.put("metadatas", metadatas);

            try {
                String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
                restClient.post()
                        .uri("/api/v1/collections/{collectionId}/upsert", collectionId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(jsonPayload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to upsert text batch for expert: " + expert, ex);
            }
        }
        return chunks.size();
    }

    private String getOrCreateCollection(String expert) {
        try {
            // Try to GET collection by name
            Map response = restClient.get()
                    .uri("/api/v1/collections/{name}", expert)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.containsKey("id")) {
                return (String) response.get("id");
            }
        } catch (HttpClientErrorException e) {
            // Collection does not exist, fall through to create
        }

        try {
            // Create collection
            Map<String, String> body = new HashMap<>();
            body.put("name", expert);
            
            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);

            Map createResponse = restClient.post()
                    .uri("/api/v1/collections")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(Map.class);

            if (createResponse != null && createResponse.containsKey("id")) {
                return (String) createResponse.get("id");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create collection for expert: " + expert, ex);
        }
        
        throw new RuntimeException("Could not get or create collection for expert: " + expert);
    }
}
