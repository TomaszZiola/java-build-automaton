package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class WebhookSignatureFilterTest extends BaseUnit {
  private static final String WEBHOOK_PATH = "/webhook";
  private static final String API_PATH = "/api/projects";
  private static final String POST = "POST";

  @Test
  @DisplayName("Non-webhook requests are forwarded without validation")
  void nonWebhookBypassesValidation() throws Exception {
    // given
    httpServletRequest.setMethod("GET");
    httpServletRequest.setRequestURI(API_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256Header, body);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("GET " + WEBHOOK_PATH + " bypasses validation (non-POST)")
  void webhookGetBypassesValidation() throws Exception {
    // given
    httpServletRequest.setMethod("GET");
    httpServletRequest.setRequestURI(WEBHOOK_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256Header, body);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("POST non-webhook path bypasses validation")
  void postNonWebhookBypassesValidation() throws Exception {
    // given
    httpServletRequest.setMethod(POST);
    httpServletRequest.setRequestURI(API_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256Header, body);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"sha1=abc", "sha256=zz", "sha256=12"})
  @DisplayName(POST + " " + WEBHOOK_PATH + " with invalid header returns 401 and stops chain")
  void invalidHeaderReturns401(final String header) throws Exception {
    // given
    httpServletRequest.setMethod(POST);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    httpServletRequest.setContent(body);
    if (header != null) {
      httpServletRequest.addHeader("X-Hub-Signature-256", header);
      when(webhookSecurityService.isSignatureValid(eq(header), any())).thenReturn(false);
    } else {
      when(webhookSecurityService.isSignatureValid(isNull(), any())).thenReturn(false);
    }

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    assertThat(httpServletResponse.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(HttpServletRequest.class), eq(httpServletResponse));
  }

  @Test
  @DisplayName(POST + " " + WEBHOOK_PATH + " with valid signature forwards request and wraps it")
  void validSignatureForwardsAndWraps() throws Exception {
    // given
    httpServletRequest.setMethod(POST);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    httpServletRequest.setContent(body);
    httpServletRequest.addHeader("X-Hub-Signature-256", validSha256Header);

    when(webhookSecurityService.isSignatureValid(eq(validSha256Header), any())).thenReturn(true);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    final var requestCaptor = forClass(HttpServletRequest.class);
    verify(filterChain, times(1)).doFilter(requestCaptor.capture(), eq(httpServletResponse));
    final HttpServletRequest wrapped = requestCaptor.getValue();
    assertThat(wrapped).isNotSameAs(httpServletRequest);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      POST
          + " "
          + WEBHOOK_PATH
          + " with valid signature allows downstream to read body (repeatable)")
  void validSignatureAllowsDownstreamToReadBody() throws Exception {
    // given
    httpServletRequest.setMethod(POST);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    final String bodyJson = "{\"msg\":\"hi\"}";
    httpServletRequest.setContent(bodyJson.getBytes(UTF_8));
    httpServletRequest.addHeader("X-Hub-Signature-256", validSha256Header);

    final var chain = new BodyReadingChain();

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, chain);

    // then
    assertThat(chain.capturedRequest).isInstanceOf(HttpServletRequest.class);
    assertThat(chain.capturedRequest).isNotSameAs(httpServletRequest);
    assertThat(chain.capturedBody).isEqualTo(bodyJson);
  }

  private static final class BodyReadingChain implements FilterChain {
    ServletRequest capturedRequest;
    String capturedBody;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response)
        throws IOException {
      this.capturedRequest = request;
      final var bytes = request.getInputStream().readAllBytes();
      this.capturedBody = new String(bytes, UTF_8);
    }
  }
}
