package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.ALLOW;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.NON_TRIGGER_REF;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IngestionGuardTest extends BaseUnit {

  @Test
  @DisplayName("Given duplicate delivery, when checking, then return DUPLICATE")
  void shouldReturnDuplicate_whenNotFirstSeen() {
    // given
    when(requestHeaderAccessor.deliveryId()).thenReturn("id");
    when(idempotencyService.firstSeen("id")).thenReturn(false);

    // when & then
    assertThat(ingestionGuardImpl.check("refs/heads/main")).isEqualTo(DUPLICATE);
  }

  @Test
  @DisplayName("Given non-trigger ref, when checking, then return NON_TRIGGER_REF")
  void shouldReturnNonTrigger_whenRefNotAllowed() {
    // given
    when(requestHeaderAccessor.deliveryId()).thenReturn("id");
    when(branchPolicy.isTriggerRef("refs/heads/feat")).thenReturn(false);

    // when & then
    assertThat(ingestionGuardImpl.check("refs/heads/feat")).isEqualTo(NON_TRIGGER_REF);
  }

  @Test
  @DisplayName("Given first-seen and trigger ref, when checking, then return ALLOW")
  void shouldReturnAllow_whenOk() {
    // when & then
    assertThat(ingestionGuardImpl.check("refs/heads/main")).isEqualTo(ALLOW);
  }
}
