package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.constants.FilterOrders.WEBHOOK_SIGNATURE;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(WEBHOOK_SIGNATURE)
public class WebhookSignatureFilter extends OncePerRequestFilter {

  private static final String WEBHOOK_PATH = "/webhook";
  private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";
  private static final String HTTP_METHOD = "POST";

  private final WebhookSecurityService security;

  public WebhookSignatureFilter(final WebhookSecurityService security) {
    this.security = security;
  }

  @Override
  protected boolean shouldNotFilter(final HttpServletRequest request) {
    return !(HTTP_METHOD.equalsIgnoreCase(request.getMethod())
        && WEBHOOK_PATH.equals(request.getRequestURI()));
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    final var bodyBytes = request.getInputStream().readAllBytes();
    final var signature = request.getHeader(SIGNATURE_HEADER);

    if (!security.isSignatureValid(signature, bodyBytes)) {
      response.sendError(SC_UNAUTHORIZED, "Invalid webhook signature");
      return;
    }

    final var repeatable = new CachedBodyRequestWrapper(request, bodyBytes);
    filterChain.doFilter(repeatable, response);
  }

  static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    CachedBodyRequestWrapper(final HttpServletRequest request, final byte[] cachedBody) {
      super(request);
      this.cachedBody = (cachedBody != null) ? cachedBody : new byte[0];
    }

    @Override
    public ServletInputStream getInputStream() {
      final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
      return new ServletInputStream() {
        private boolean isDone;

        @Override
        public int read() {
          final int valueByte = bais.read();
          isDone = (bais.available() == 0);
          return valueByte;
        }

        @Override
        public boolean isFinished() {
          return isDone;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {}
      };
    }

    @Override
    public String getCharacterEncoding() {
      return (super.getCharacterEncoding() != null) ? super.getCharacterEncoding() : UTF_8.name();
    }

    @Override
    public int getContentLength() {
      return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
      return cachedBody.length;
    }
  }
}
