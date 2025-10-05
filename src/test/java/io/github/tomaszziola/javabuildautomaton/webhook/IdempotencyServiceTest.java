package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import io.github.tomaszziola.javabuildautomaton.webhook.entity.WebhookDelivery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class IdempotencyServiceTest extends BaseUnit {

  @Test
  @DisplayName("Given null or blank delivery id, when checking firstSeen, then return true")
  void shouldReturnTrue_whenNullOrBlank() {

    // when & then
    assertThat(idempotencyServiceImpl.isDuplicateWebhook(null)).isFalse();
    assertThat(idempotencyServiceImpl.isDuplicateWebhook(" ")).isFalse();
  }

  @Test
  @DisplayName("Given new delivery id, when checking firstSeen, then persist and return true")
  void shouldPersistAndReturnTrue_whenNewId() {
    // given
    final var id = "abc";
    final var result = idempotencyServiceImpl.isDuplicateWebhook(id);

    // when & then
    assertThat(result).isFalse();
    verify(webhookDeliveryRepository).save(any(WebhookDelivery.class));
  }

  @Test
  @DisplayName("Given duplicate delivery id, when checking firstSeen, then return false")
  void shouldReturnFalse_whenDuplicate() {
    // given
    final var id = "duplicated";
    doThrow(new DataIntegrityViolationException("duplicated"))
        .when(webhookDeliveryRepository)
        .save(any(WebhookDelivery.class));

    final var result = idempotencyServiceImpl.isDuplicateWebhook(id);

    // when & then
    assertThat(result).isTrue();
  }
}
