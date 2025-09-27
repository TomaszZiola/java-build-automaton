package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookIngestionServiceTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given NON_TRIGGER_REF, when handling webhook, then return NOT_FOUND with ignore message")
  void shouldReturnNotFound_whenNonTriggerRef() {
    // given
    when(ingestionGuard.check(payload.ref())).thenReturn(NON_TRIGGER_REF);

    // when
    final var result = webhookIngestionServiceImpl.handleWebhook(payload);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message()).isEqualTo("Ignoring event for ref: " + payload.ref());
  }

  @Test
  @DisplayName(
      "Given DUPLICATE, when handling webhook, then return NOT_FOUND with duplicate message")
  void shouldReturnNotFound_whenDuplicateDelivery() {
    // given
    when(ingestionGuard.check(payload.ref())).thenReturn(DUPLICATE);

    // when
    final var result = webhookIngestionServiceImpl.handleWebhook(payload);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message()).isEqualTo("Duplicate delivery ignored");
  }

  @Test
  @DisplayName("Given existing project, when handling webhook, then enqueue build and return FOUND")
  void shouldEnqueueBuild_whenProjectFound() {
    // given
    when(ingestionGuard.check(payload.ref())).thenReturn(ALLOW);

    // when
    final var result = webhookIngestionServiceImpl.handleWebhook(payload);

    // then
    assertThat(result.status()).isEqualTo(FOUND);
    verify(buildOrchestrator).enqueueBuild(project);
  }

  @Test
  @DisplayName("Given missing project, when handling webhook, then return NOT_FOUND")
  void shouldReturnNotFound_whenProjectMissing() {
    // given
    when(ingestionGuard.check(payload.ref())).thenReturn(ALLOW);
    when(projectRepository.findByRepositoryName(payload.repository().fullName()))
        .thenReturn(empty());

    // when
    final var result = webhookIngestionServiceImpl.handleWebhook(payload);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message())
        .isEqualTo("Project not found for repository: " + payload.repository().fullName());
  }
}
