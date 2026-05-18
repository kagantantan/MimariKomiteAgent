package com.mimari.komite.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ConsultResponse {
    private String question;
    private String synthesis;
    private Map<String, String> expertAnswers;
}
