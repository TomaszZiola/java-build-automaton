package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tomaszziola.javabuildautomaton.models.WebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.WebhookPayloadWithHeadersModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BranchPolicyTest extends BaseUnit {

  @Test
  @DisplayName("Given main/master refs, when checking policy, then trigger")
  void shouldTrigger_whenMainOrMaster() {
    // when & then
    assertThat(branchPolicyImpl.isTriggerRef(payloadWithHeaders)).isTrue();
  }

  @Test
  @DisplayName("Given feature ref, when checking policy, then do not trigger")
  void shouldNotTrigger_whenFeatureRef() {
    // given
    payload = WebhookPayloadModel.builder().ref("refs/heads/feature/abc").build();
    payloadWithHeaders = WebhookPayloadWithHeadersModel.builder().payload(payload).build();

    // when & then
    assertThat(branchPolicyImpl.isTriggerRef(payloadWithHeaders)).isFalse();
  }

  @Test
  @DisplayName("Given not supported event type, when checking policy, then do not trigger")
  void shouldNotTrigger_whenNotSupportedEvent() {
    // given
    payloadWithHeaders =
        WebhookPayloadWithHeadersModel.builder().payload(payload).event("not_supported").build();

    // when & then
    assertThat(branchPolicyImpl.isTriggerRef(payloadWithHeaders)).isFalse();
  }
}
