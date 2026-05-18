package com.mimari.komite.api;

import com.mimari.komite.knowledge.KnowledgeIngestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService ingestionService;

    public KnowledgeController(KnowledgeIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody IngestRequest request) throws IOException {
        int chunks = ingestionService.ingestPdf(request.expert(), request.filePath(), request.sourceName());
        return Map.of("expert", request.expert(), "source", request.sourceName(), "status", "success", "chunksLoaded", chunks);
    }

    @PostMapping("/ingest-pdf")
    public Map<String, Object> ingestPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("expert") String expert,
            @RequestParam("sourceName") String sourceName) throws IOException {
        Path temp = Files.createTempFile("upload-", ".pdf");
        Files.copy(file.getInputStream(), temp, StandardCopyOption.REPLACE_EXISTING);
        int chunks = ingestionService.ingestPdf(expert, temp.toString(), sourceName);
        Files.deleteIfExists(temp);
        return Map.of("expert", expert, "source", sourceName, "status", "success", "chunksLoaded", chunks);
    }

    @PostMapping("/ingest-text")
    public Map<String, Object> ingestText(@RequestBody IngestTextRequest request) throws IOException {
        int chunks = ingestionService.ingestText(request.expert(), request.text(), request.sourceName());
        return Map.of("expert", request.expert(), "source", request.sourceName(), "status", "success", "chunksLoaded", chunks);
    }

    @GetMapping("/status")
    public List<Map<String, Object>> status() {
        return List.of(
            Map.of("expert", "eric-evans", "name", "Eric Evans", "category", "DDD"),
            Map.of("expert", "vaughn-vernon", "name", "Vaughn Vernon", "category", "DDD"),
            Map.of("expert", "vlad-khononov", "name", "Vlad Khononov", "category", "DDD"),
            Map.of("expert", "mathias-verraes", "name", "Mathias Verraes", "category", "DDD"),
            Map.of("expert", "alberto-brandolini", "name", "Alberto Brandolini", "category", "DDD"),
            Map.of("expert", "nick-tune", "name", "Nick Tune", "category", "DDD"),
            Map.of("expert", "sam-newman", "name", "Sam Newman", "category", "Microservices"),
            Map.of("expert", "chris-richardson", "name", "Chris Richardson", "category", "Microservices"),
            Map.of("expert", "martin-fowler", "name", "Martin Fowler", "category", "Patterns"),
            Map.of("expert", "michael-feathers", "name", "Michael Feathers", "category", "Legacy"),
            Map.of("expert", "martin-kleppmann", "name", "Martin Kleppmann", "category", "Data"),
            Map.of("expert", "pramod-sadalage", "name", "Pramod Sadalage", "category", "Data"),
            Map.of("expert", "neal-ford-mark-richards", "name", "Ford & Richards", "category", "Architecture"),
            Map.of("expert", "kent-beck", "name", "Kent Beck", "category", "XP"),
            Map.of("expert", "uncle-bob", "name", "Uncle Bob", "category", "Clean Code")
        );
    }

    public record IngestRequest(String expert, String filePath, String sourceName) {}
    public record IngestTextRequest(String expert, String text, String sourceName) {}
}
