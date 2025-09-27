package io.github.tomaszziola.javabuildautomaton.config;

import static io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter.CORRELATION_ID_HEADER;
import static io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class CorrelationIdFilterTest extends BaseUnit {

  private static final class CapturingChain implements FilterChain {
    String mdcValue;
    boolean invoked;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) {
      invoked = true;
      mdcValue = MDC.get(MDC_KEY);
    }
  }

  @Test
  @DisplayName("Given header present, when filtering, then use header and clean MDC")
  void usesHeaderAndCleansMdcWhenHeaderPresent() throws ServletException, IOException {
    // given
    httpServletRequest.addHeader(CORRELATION_ID_HEADER, incomingId);
    final var chain = new CapturingChain();

    // when
    correlationIdFilter.doFilter(httpServletRequest, httpServletResponse, chain);

    // then
    assertThat(chain.invoked).isTrue();
    assertThat(chain.mdcValue).isEqualTo(incomingId);
    assertThat(httpServletResponse.getHeader(CORRELATION_ID_HEADER)).isEqualTo(incomingId);
    assertThat(MDC.get(MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Given header missing, when filtering, then generate UUID and clean MDC")
  void generatesUuidAndCleansMdcWhenHeaderMissing() throws ServletException, IOException {
    // given
    final var chain = new CapturingChain();

    // when
    correlationIdFilter.doFilter(httpServletRequest, httpServletResponse, chain);

    // then
    final String headerValue = httpServletResponse.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
    assertThat(chain.mdcValue).isEqualTo(headerValue);
    assertThat(MDC.get(MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Given header blank, when filtering, then generate UUID and clean MDC")
  void generatesUuidAndCleansMdcWhenHeaderBlank() throws ServletException, IOException {
    // given
    httpServletRequest.addHeader(CORRELATION_ID_HEADER, "   ");

    // when
    correlationIdFilter.doFilter(httpServletRequest, httpServletResponse, new CapturingChain());

    // then
    final String headerValue = httpServletResponse.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
  }

  private static void assertValidUuid(final String value) {
    try {
      UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      fail("Correlation ID is not a isValid UUID: " + value);
    }
  }
}
