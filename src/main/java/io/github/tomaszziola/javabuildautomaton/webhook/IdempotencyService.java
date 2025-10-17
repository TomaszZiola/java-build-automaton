package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.webhook.entity.WebhookDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

  private final WebhookDeliveryRepository repository;

  public boolean isDuplicate(String deliveryId) {
    return !tryRegisterFirstSeen(deliveryId);
  }

  private boolean tryRegisterFirstSeen(String deliveryId) {
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
}
