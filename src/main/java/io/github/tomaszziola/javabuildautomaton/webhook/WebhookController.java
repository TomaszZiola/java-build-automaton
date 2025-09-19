package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

  private final WebhookIngestionService webhookIngestionService;

  public WebhookController(final WebhookIngestionService webhookIngestionService) {
    this.webhookIngestionService = webhookIngestionService;
  }

  @PostMapping("/webhook")
  public ApiResponse handleWebhook(@RequestBody final GitHubWebhookPayload payload) {
    return webhookIngestionService.handleWebhook(payload);
  }
}
