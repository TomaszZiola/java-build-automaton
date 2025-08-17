package io.github.tomaszziola.javabuildautomaton;

import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebhookController {

    private final ProjectService service;

    public WebhookController(ProjectService service) {
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse> handleWebhook(@RequestBody GitHubWebhookPayload payload) {
        ApiResponse response = service.handleProjectLookup(payload);
        return ResponseEntity.ok(response);
    }
}
