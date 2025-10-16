package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookRestControllerTest extends BaseUnit {

  @Test
  @DisplayName("Given isValid payload, when handling webhook, then return OK response")
  void returnsOkResponseWhenPayloadValid() {
    // when
    var result =
        webhookRestControllerImpl.handleWebhook(
            "a65977c0-aa67-11f0-9e18-5a74c246d36d", "pull_request", payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }
}
