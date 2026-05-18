package com.mimari.komite.knowledge;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChromaSearchService {

    private final EmbeddingModel embeddingModel;
    private final RestClient restClient;

    public ChromaSearchService(EmbeddingModel embeddingModel, RestClient.Builder restClientBuilder) {
        this.embeddingModel = embeddingModel;
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        this.restClient = restClientBuilder
                .requestFactory(factory)
                .baseUrl(System.getenv("CHROMA_URL") != null ? System.getenv("CHROMA_URL") : "http://localhost:8000")
                .build();
    }

    public List<String> search(String collectionName, String query, int topK) {
        String collectionId = null;
        try {
            Map response = restClient.get()
                    .uri("/api/v1/collections/{name}", collectionName)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.containsKey("id")) {
                collectionId = (String) response.get("id");
            }
        } catch (HttpClientErrorException e) {
            return java.util.Collections.emptyList();
        }

        if (collectionId == null) {
            return new ArrayList<>();
        }

        float[] queryEmbedding = embeddingModel.embed(query);

        Map<String, Object> payload = new HashMap<>();
        payload.put("query_embeddings", List.of(queryEmbedding));
        payload.put("n_results", topK);
        payload.put("include", List.of("documents"));

        try {
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            Map queryResponse = restClient.post()
                    .uri("/api/v1/collections/{collectionId}/query", collectionId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(jsonPayload)
                    .retrieve()
                    .body(Map.class);

            if (queryResponse != null && queryResponse.containsKey("documents")) {
                List<List<String>> docs = (List<List<String>>) queryResponse.get("documents");
                if (docs != null && !docs.isEmpty()) {
                    return docs.get(0);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to query collection: " + collectionName, ex);
        }

        return new ArrayList<>();
    }
}
