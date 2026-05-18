package com.mimari.komite.api;

import com.mimari.komite.model.ConsultRequest;
import com.mimari.komite.model.ConsultResponse;
import com.mimari.komite.orchestrator.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final OrchestratorService orchestratorService;

    @PostMapping
    public ConsultResponse consult(@RequestBody ConsultRequest request) {
        return orchestratorService.orchestrateConsultation(request.getQuestion());
    }
}
