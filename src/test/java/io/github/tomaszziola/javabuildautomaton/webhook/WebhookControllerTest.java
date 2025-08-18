package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnitTest;
import org.junit.jupiter.api.Test;

public class WebhookControllerTest extends BaseUnitTest {

  @Test
  void givenValidPayload_whenHandleWebhook_thenReturnOkResponse() {
    // when
    var result = webhookControllerImpl.handleWebhook(payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }
}
