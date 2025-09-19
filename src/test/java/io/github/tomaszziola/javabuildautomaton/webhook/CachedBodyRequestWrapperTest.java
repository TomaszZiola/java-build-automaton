package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.ServletInputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CachedBodyRequestWrapperTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given cached request wrapper, when reading input stream, then read all bytes and mark finished")
  void readsAllBytesAndMarksFinished() throws IOException {
    // given
    final var wrapper =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, bodyBytes);

    // when
    try (ServletInputStream inputStream = wrapper.getInputStream()) {
      // then
      assertThat(inputStream.isReady()).isTrue();
      inputStream.setReadListener(null);
      assertThat(inputStream.isFinished()).isFalse();

      final byte[] read = inputStream.readAllBytes();
      assertThat(read).isEqualTo(bodyBytes);
      assertThat(inputStream.isFinished()).isTrue();
    }

    assertThat(wrapper.getContentLength()).isEqualTo(bodyBytes.length);
    assertThat(wrapper.getContentLengthLong()).isEqualTo(bodyBytes.length);
  }

  @Test
  @DisplayName(
      "Given request encoding absent or set, when getting character encoding, then return request encoding or UTF-8")
  void returnsRequestEncodingOrUtf8() {
    final var wrapperNoEnc =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, bodyBytes);

    assertThat(wrapperNoEnc.getCharacterEncoding()).isEqualTo(UTF_8.name());

    httpServletRequest.setCharacterEncoding(ISO_8859_1.name());
    final var wrapperWithEnc =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, bodyBytes);

    assertThat(wrapperWithEnc.getCharacterEncoding()).isEqualTo(ISO_8859_1.name());
  }
}
