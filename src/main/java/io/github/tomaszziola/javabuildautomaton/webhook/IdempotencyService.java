package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.webhook.entity.WebhookDelivery;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

  private final WebhookDeliveryRepository repository;

  public IdempotencyService(final WebhookDeliveryRepository repository) {
    this.repository = repository;
  }

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

  public boolean isDuplicate(final String deliveryId) {
    return !registerIfFirstSeen(deliveryId);
  }
}
