package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.webhook.entity.WebhookDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

  private final WebhookDeliveryRepository repository;

  private boolean registerIfFirstSeen(final String deliveryId) {
    if (deliveryId == null || deliveryId.isBlank()) {
      return true;
    }
    try {
      repository.save(WebhookDelivery.builder().deliveryId(deliveryId).build());
      return true;
    } catch (DataIntegrityViolationException exception) {
      return false;
    }
  }

  public boolean isDuplicateWebhook(final String deliveryId) {
    return !registerIfFirstSeen(deliveryId);
  }
}
