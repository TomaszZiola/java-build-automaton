package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionGuard {

  private final BranchPolicy branchPolicy;
  private final IdempotencyService idempotencyService;
  private final RequestHeaderAccessor requestHeaderAccessor;

  public IngestionGuardResult evaluateIngestion(final String ref) {
    final var deliveryId = requestHeaderAccessor.deliveryId();
    if (idempotencyService.isDuplicateWebhook(deliveryId)) {
      return DUPLICATE;
    }
    if (branchPolicy.isNonTriggerRef(ref)) {
      return NON_TRIGGER_REF;
    }
    return ALLOW;
  }
}
