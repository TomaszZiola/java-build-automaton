package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;

import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayloadWithHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionGuard {

  private final BranchPolicy branchPolicy;
  private final IdempotencyService idempotencyService;

  public IngestionGuardResult evaluateIngestion(WebhookPayloadWithHeaders payload) {
    var deliveryId = payload.deliveryId();
    if (idempotencyService.isDuplicateWebhook(deliveryId)) {
      return DUPLICATE;
    }
    if (!branchPolicy.isTriggerRef(payload)) {
      return NON_TRIGGER_REF;
    }
    return ALLOW;
  }
}
