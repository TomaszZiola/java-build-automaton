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

  @Test
  @DisplayName("Given non-webhook request, when filtering, then forward without validation")
  void forwardsWithoutValidationForNonWebhookRequest() throws Exception {
    // given
    httpServletRequest.setMethod("GET");
    httpServletRequest.setRequestURI(API_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256HeaderValue, bodyBytes);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("Given GET /webhook request, when filtering, then forward without validation")
  void forwardsWithoutValidationForGetWebhook() throws Exception {
    // given
    httpServletRequest.setMethod("GET");
    httpServletRequest.setRequestURI(WEBHOOK_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256HeaderValue, bodyBytes);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("Given POST non-webhook path, when filtering, then forward without validation")
  void forwardsWithoutValidationForPostNonWebhookPath() throws Exception {
    // given
    httpServletRequest.setMethod(postMethod);
    httpServletRequest.setRequestURI(API_PATH);

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(validSha256HeaderValue, bodyBytes);
    verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    assertThat(httpServletResponse.getStatus()).isEqualTo(200);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"sha1=abc", "sha256=zz", "sha256=12"})
  @DisplayName(
      "Given POST /webhook with invalid header {0}, when filtering, then return 401 and stop chain")
  void returns401AndStopsChainForInvalidHeader(final String header) throws Exception {
    // given
    httpServletRequest.setMethod(postMethod);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    httpServletRequest.setContent(bodyBytes);
    if (header != null) {
      httpServletRequest.addHeader(validSha256HeaderName, header);
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
  @DisplayName(
      "Given POST /webhook with isValid signature, when filtering, then forward request and wrap it")
  void forwardsAndWrapsForValidSignature() throws Exception {
    // given
    httpServletRequest.setMethod(postMethod);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    httpServletRequest.setContent(bodyBytes);
    httpServletRequest.addHeader(validSha256HeaderName, validSha256HeaderValue);

    when(webhookSecurityService.isSignatureValid(eq(validSha256HeaderValue), any()))
        .thenReturn(true);

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
      "Given POST /webhook with isValid signature, when filtering, then allow downstream to read body")
  void allowsDownstreamToReadBodyForValidSignature() throws Exception {
    // given
    httpServletRequest.setMethod(postMethod);
    httpServletRequest.setRequestURI(WEBHOOK_PATH);
    httpServletRequest.setContent(bodyJson.getBytes(UTF_8));
    httpServletRequest.addHeader(validSha256HeaderName, validSha256HeaderValue);

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
