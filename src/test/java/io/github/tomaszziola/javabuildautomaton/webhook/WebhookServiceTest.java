package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.SKIPPED;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookServiceTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given NON_TRIGGER_REF, when handling webhook, then return NOT_FOUND with ignore message")
  void shouldReturnNotFound_whenNonTriggerRef() {
    // given
    when(ingestionGuard.evaluate(payloadWithHeaders)).thenReturn(NON_TRIGGER_REF);

    // when
    var result = webhookServiceImpl.handle(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(SKIPPED);
    assertThat(result.message())
        .isEqualTo("Non triggered ref ignored for Deliver ID: " + payloadWithHeaders.deliveryId());
  }

  @Test
  @DisplayName("Given DUPLICATE, when handling webhook, then return SKIPPED with duplicate message")
  void shouldReturnNotFound_whenDuplicateDelivery() {
    // given
    when(ingestionGuard.evaluate(payloadWithHeaders)).thenReturn(DUPLICATE);

    // when
    var result = webhookServiceImpl.handle(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(SKIPPED);
    assertThat(result.message())
        .isEqualTo("Duplicate delivery ignored for Deliver ID: " + payloadWithHeaders.deliveryId());
  }

  @Test
  @DisplayName("Given existing project, when handling webhook, then enqueue build and return FOUND")
  void shouldEnqueueBuild_whenProjectFound() {
    // given
    when(ingestionGuard.evaluate(payloadWithHeaders)).thenReturn(ALLOW);

    // when
    var result = webhookServiceImpl.handle(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(FOUND);
    verify(buildOrchestrator).enqueue(project);
  }

  @Test
  @DisplayName("Given missing project, when handling webhook, then return NOT_FOUND")
  void shouldReturnNotFound_whenProjectMissing() {
    // given
    when(ingestionGuard.evaluate(payloadWithHeaders)).thenReturn(ALLOW);
    when(projectRepository.findByRepositoryFullName(payload.repository().fullName()))
        .thenReturn(empty());

    // when
    var result = webhookServiceImpl.handle(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message())
        .isEqualTo("Project not found for repositoryFullName: " + payload.repository().fullName());
  }
}
