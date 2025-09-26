package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;

import org.springframework.stereotype.Component;

@Component
public class IngestionGuard {

  private final BranchPolicy branchPolicy;
  private final IdempotencyService idempotencyService;
  private final RequestHeaderAccessor requestHeaderAccessor;

  public IngestionGuard(
      final BranchPolicy branchPolicy,
      final IdempotencyService idempotencyService,
      final RequestHeaderAccessor requestHeaderAccessor) {
    this.branchPolicy = branchPolicy;
    this.idempotencyService = idempotencyService;
    this.requestHeaderAccessor = requestHeaderAccessor;
  }

  public IngestionGuardResult check(final String ref) {
    final var deliveryId = requestHeaderAccessor.deliveryId();
    if (idempotencyService.isDuplicate(deliveryId)) {
      return DUPLICATE;
    }
    if (branchPolicy.isNonTriggerRef(ref)) {
      return NON_TRIGGER_REF;
    }
    return ALLOW;
  }
}
