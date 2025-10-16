package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.JAVA_OUTDATED;
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
    when(ingestionGuard.evaluateIngestion(payloadWithHeaders)).thenReturn(NON_TRIGGER_REF);

    // when
    var result = webhookIngestionServiceImpl.handleWebhook(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message()).isEqualTo("Ignoring event for ref: " + payload.ref());
  }

  @Test
  @DisplayName(
      "Given DUPLICATE, when handling webhook, then return NOT_FOUND with duplicate message")
  void shouldReturnNotFound_whenDuplicateDelivery() {
    // given
    when(ingestionGuard.evaluateIngestion(payloadWithHeaders)).thenReturn(DUPLICATE);

    // when
    var result = webhookIngestionServiceImpl.handleWebhook(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message()).isEqualTo("Duplicate delivery ignored");
  }

  @Test
  @DisplayName("Given existing project, when handling webhook, then enqueue build and return FOUND")
  void shouldEnqueueBuild_whenProjectFound() {
    // given
    when(ingestionGuard.evaluateIngestion(payloadWithHeaders)).thenReturn(ALLOW);

    // when
    var result = webhookIngestionServiceImpl.handleWebhook(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(FOUND);
    verify(buildOrchestrator).enqueueBuild(project);
  }

  @Test
  @DisplayName("Given missing project, when handling webhook, then return NOT_FOUND")
  void shouldReturnNotFound_whenProjectMissing() {
    // given
    when(ingestionGuard.evaluateIngestion(payloadWithHeaders)).thenReturn(ALLOW);
    when(projectRepository.findByRepositoryFullName(payload.repository().fullName()))
        .thenReturn(empty());

    // when
    var result = webhookIngestionServiceImpl.handleWebhook(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message())
        .isEqualTo("Project not found for repositoryFullName: " + payload.repository().fullName());
  }

  @Test
  @DisplayName("Given Java version too old, when handling webhook, then return JAVA_OUTDATED")
  void shouldReturnNotFound_whenUnhandledOutcome() {
    // given
    when(ingestionGuard.evaluateIngestion(payloadWithHeaders)).thenReturn(JAVA_OUTDATED);

    // when
    var result = webhookIngestionServiceImpl.handleWebhook(payloadWithHeaders);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message()).isEqualTo("Unhandled outcome: " + JAVA_OUTDATED);
  }
}
