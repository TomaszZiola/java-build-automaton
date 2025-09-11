package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.NON_TRIGGER_REF;

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

  public enum Outcome {
    ALLOW,
    DUPLICATE,
    NON_TRIGGER_REF
  }

  public Outcome check(final String ref) {
    final var deliveryId = requestHeaderAccessor.deliveryId();
    if (!idempotencyService.firstSeen(deliveryId)) {
      return DUPLICATE;
    }
    if (!branchPolicy.isTriggerRef(ref)) {
      return NON_TRIGGER_REF;
    }
    return ALLOW;
  }
}
