package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.constants.HeadersConstants.X_GITHUB_DELIVERY;
import static io.github.tomaszziola.javabuildautomaton.constants.HeadersConstants.X_GITHUB_EVENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayloadWithHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookRestController {

  private final WebhookService webhookService;

  @PostMapping("/webhook")
  public ApiResponse handleWebhook(
      @RequestHeader(X_GITHUB_DELIVERY) String deliveryId,
      @RequestHeader(X_GITHUB_EVENT) String eventType,
      @RequestBody WebhookPayload payload) {
    var webhookRequest = new WebhookPayloadWithHeaders(payload, deliveryId, eventType);
    return webhookService.handle(webhookRequest);
  }
}
