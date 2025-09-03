package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookControllerTest extends BaseUnit {

  @Test
  @DisplayName("Given valid payload, when handling webhook, then return OK response")
  void returnsOkResponseWhenPayloadValid() {
    // when
    final var result = webhookControllerImpl.handleWebhook(payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }
}
