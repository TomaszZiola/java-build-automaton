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
  void givenHeaderPresent_whenDoFilter_thenUsesHeaderAndCleansUpMdc()
      throws ServletException, IOException {
    // given
    request.addHeader(CORRELATION_ID_HEADER, incomingId);
    final var chain = new CapturingChain();

    // when
    filterImpl.doFilter(request, response, chain);

    // then
    assertThat(chain.invoked).isTrue();
    assertThat(chain.mdcValue).isEqualTo(incomingId);
    assertThat(response.getHeader(CORRELATION_ID_HEADER)).isEqualTo(incomingId);
    assertThat(MDC.get(MDC_KEY)).isNull();
  }

  @Test
  void givenHeaderMissing_whenDoFilter_thenGeneratesUuidAndCleansUpMdc()
      throws ServletException, IOException {
    // given
    final var chain = new CapturingChain();

    // when
    filterImpl.doFilter(request, response, chain);

    // then
    final String headerValue = response.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
    assertThat(chain.mdcValue).isEqualTo(headerValue);
    assertThat(MDC.get(MDC_KEY)).isNull();
  }

  @Test
  void givenHeaderBlank_whenDoFilter_thenGeneratesUuidAndCleansUpMdc()
      throws ServletException, IOException {
    // given
    request.addHeader(CORRELATION_ID_HEADER, "   ");

    // when
    filterImpl.doFilter(request, response, new CapturingChain());

    // then
    final String headerValue = response.getHeader(CORRELATION_ID_HEADER);
    assertThat(headerValue).isNotBlank();
    assertValidUuid(headerValue);
  }

  private static void assertValidUuid(final String value) {
    try {
      UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      fail("Correlation ID is not a valid UUID: " + value);
    }
  }
}
