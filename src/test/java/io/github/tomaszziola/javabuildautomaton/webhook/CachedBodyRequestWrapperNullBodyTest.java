package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.ServletInputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CachedBodyRequestWrapperNullBodyTest extends BaseUnit {

  @Test
  @DisplayName("Given null body, when creating wrapper, then return empty stream and zero lengths")
  void returnsEmptyStreamAndZeroLengthsWhenBodyNull() throws IOException {
    // given
    final var wrapper =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequestImpl, null);

    // when
    try (ServletInputStream inputStream = wrapper.getInputStream()) {
      // then
      assertThat(inputStream.isFinished()).isFalse();
      assertThat(inputStream.readAllBytes()).isEmpty();
      assertThat(inputStream.isFinished()).isTrue();
    }
    assertThat(wrapper.getContentLength()).isZero();
    assertThat(wrapper.getContentLengthLong()).isZero();
  }
}
