package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

  private final ProjectService service;

  public WebhookController(final ProjectService service) {
    this.service = service;
  }

  @PostMapping("/webhook")
  public ResponseEntity<ApiResponse> handleWebhook(
      @RequestBody final GitHubWebhookPayload payload) {
    final ApiResponse response = service.handleProjectLookup(payload);
    return ResponseEntity.ok(response);
  }
}
