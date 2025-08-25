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
  @DisplayName("getInputStream reads all bytes and isFinished transitions to true")
  void inputStreamReadsAllAndIsFinished() throws IOException {
    // given
    final var wrapper =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, body);

    // when
    try (ServletInputStream inputStream = wrapper.getInputStream()) {
      // then
      assertThat(inputStream.isReady()).isTrue();
      inputStream.setReadListener(null);
      assertThat(inputStream.isFinished()).isFalse();

      final byte[] read = inputStream.readAllBytes();
      assertThat(read).isEqualTo(body);
      assertThat(inputStream.isFinished()).isTrue();
    }

    assertThat(wrapper.getContentLength()).isEqualTo(body.length);
    assertThat(wrapper.getContentLengthLong()).isEqualTo(body.length);
  }

  @Test
  @DisplayName("getCharacterEncoding returns request encoding or falls back to UTF-8")
  void characterEncodingFallbackAndPreserve() {
    final var wrapperNoEnc =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, body);

    assertThat(wrapperNoEnc.getCharacterEncoding()).isEqualTo(UTF_8.name());

    httpServletRequest.setCharacterEncoding(ISO_8859_1.name());
    final var wrapperWithEnc =
        new WebhookSignatureFilter.CachedBodyRequestWrapper(httpServletRequest, body);

    assertThat(wrapperWithEnc.getCharacterEncoding()).isEqualTo(ISO_8859_1.name());
  }
}
