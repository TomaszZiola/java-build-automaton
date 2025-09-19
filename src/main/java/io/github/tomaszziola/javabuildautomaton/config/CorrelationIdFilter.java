package io.github.tomaszziola.javabuildautomaton.config;

import static io.github.tomaszziola.javabuildautomaton.constants.FilterOrders.CORRELATION_ID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(CORRELATION_ID)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    final var correlationId = getOrGenerateCorrelationId(request);

    MDC.put(MDC_KEY, correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  private String getOrGenerateCorrelationId(final HttpServletRequest request) {
    final var headerValue = request.getHeader(CORRELATION_ID_HEADER);
    if (headerValue != null && !headerValue.isBlank()) {
      return headerValue;
    }
    return UUID.randomUUID().toString();
  }
}
