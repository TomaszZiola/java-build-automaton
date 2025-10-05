package io.github.tomaszziola.javabuildautomaton.config;

import static io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter.CORRELATION_ID_HEADER;
import static io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.MDC.get;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CorrelationIdFilterTest extends BaseUnit {

  private static final class CapturingChain implements FilterChain {

    String mdcValue;
    boolean invoked;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) {
      invoked = true;
      mdcValue = get(MDC_KEY);
    }
  }

  @Test
  @DisplayName("Given header present, when filtering, then use header and clean MDC")
  void usesHeaderAndCleansMdcWhenHeaderPresent() throws ServletException, IOException {
    // given
    httpServletRequestImpl.addHeader(CORRELATION_ID_HEADER, incomingId);
    final var chain = new CapturingChain();

    // when
    correlationIdFilterImpl.doFilter(httpServletRequestImpl, httpServletResponseImpl, chain);

    // then
    assertThat(chain.invoked).isTrue();
    assertThat(chain.mdcValue).isEqualTo(incomingId);
    assertThat(httpServletResponseImpl.getHeader(CORRELATION_ID_HEADER)).isEqualTo(incomingId);
    assertThat(get(MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Given header missing, when filtering, then generate UUID and clean MDC")
  void generatesUuidAndCleansMdcWhenHeaderMissing() throws ServletException, IOException {
    // given
    final var chain = new CapturingChain();

    // when
    correlationIdFilterImpl.doFilter(httpServletRequestImpl, httpServletResponseImpl, chain);

    // then
    final String headerValue = httpServletResponseImpl.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
    assertThat(chain.mdcValue).isEqualTo(headerValue);
    assertThat(get(MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Given header blank, when filtering, then generate UUID and clean MDC")
  void generatesUuidAndCleansMdcWhenHeaderBlank() throws ServletException, IOException {
    // given
    httpServletRequestImpl.addHeader(CORRELATION_ID_HEADER, "   ");

    // when
    correlationIdFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, new CapturingChain());

    // then
    final String headerValue = httpServletResponseImpl.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
  }

  private static void assertValidUuid(final String value) {
    try {
      UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      throw new AssertionError("Correlation ID is not a valid UUID: " + value, ex);
    }
  }
}
