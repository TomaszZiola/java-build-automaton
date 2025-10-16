package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuardResult.NON_TRIGGER_REF;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.models.WebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.WebhookPayloadWithHeadersModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IngestionGuardTest extends BaseUnit {

  @Test
  @DisplayName("Given duplicate delivery, when checking, then return DUPLICATE")
  void shouldReturnDuplicate_whenNotFirstSeen() {
    // given
    when(idempotencyService.isDuplicateWebhook("a65977c0-aa67-11f0-9e18-5a74c246d36d"))
        .thenReturn(true);

    // when & then
    assertThat(ingestionGuardImpl.evaluateIngestion(payloadWithHeaders)).isEqualTo(DUPLICATE);
  }

  @Test
  @DisplayName("Given non-trigger ref, when checking, then return NON_TRIGGER_REF")
  void shouldReturnNonTrigger_whenRefNotAllowed() {
    // given
    payload = WebhookPayloadModel.builder().ref("refs/heads/feature/abc").build();
    payloadWithHeaders = WebhookPayloadWithHeadersModel.builder().payload(payload).build();
    when(branchPolicy.isTriggerRef(payloadWithHeaders)).thenReturn(false);

    // when & then
    assertThat(ingestionGuardImpl.evaluateIngestion(payloadWithHeaders)).isEqualTo(NON_TRIGGER_REF);
  }

  @Test
  @DisplayName("Given first-seen and trigger ref, when checking, then return ALLOW")
  void shouldReturnAllow_whenOk() {
    // when & then
    assertThat(ingestionGuardImpl.evaluateIngestion(payloadWithHeaders)).isEqualTo(ALLOW);
  }
}
