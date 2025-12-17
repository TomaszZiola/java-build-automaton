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
import org.junit.jupiter.params.provider.ValueSource;

class WebhookSignatureFilterTest extends BaseUnit {

  @Test
  @DisplayName("Given non-webhook request, when filtering, then forward without validation")
  void forwardsWithoutValidationForNonWebhookRequest() throws Exception {
    // given
    httpServletRequestImpl.setMethod("GET");
    httpServletRequestImpl.setRequestURI(apiPath);

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    verify(webhookSecurityService, never())
        .isSignatureValid(validSha256HeaderValue, bodyBytes, webhookSecret);
    verify(filterChain, times(1)).doFilter(httpServletRequestImpl, httpServletResponseImpl);
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Given POST /webhook with invalid JSON, when filtering, then return 400 and stop chain")
  void returns400AndStopsChainForInvalidJson() throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent("{".getBytes(UTF_8)); // malformed JSON

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(400);
    verify(filterChain, never())
        .doFilter(any(HttpServletRequest.class), eq(httpServletResponseImpl));
  }

  @Test
  @DisplayName(
      "Given POST /webhook with unknown repository, when filtering, then return 404 and stop chain")
  void returns404AndStopsChainWhenProjectNotFound() throws Exception {
    // given: payload with different full_name
    String unknownRepoJson =
        """
            {
              "repository": {
                "full_name": "unknown/other"
              }
            }
            """;
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent(unknownRepoJson.getBytes(UTF_8));

    when(projectRepository.findByRepositoryFullName("unknown/other"))
        .thenReturn(java.util.Optional.empty());

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(404);
    verify(filterChain, never())
        .doFilter(any(HttpServletRequest.class), eq(httpServletResponseImpl));
  }

  @Test
  @DisplayName(
      "Given POST /webhook with missing secret, when filtering, then return 500 and stop chain")
  void returns500AndStopsChainWhenSecretMissing() throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent(bodyJson.getBytes(UTF_8));

    // Project resolved by BaseUnit for repositoryName; make secret blank
    project.setWebhookSecret(" ");

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(500);
    verify(filterChain, never())
        .doFilter(any(HttpServletRequest.class), eq(httpServletResponseImpl));
  }

  @Test
  @DisplayName("Given GET /webhook request, when filtering, then forward without validation")
  void forwardsWithoutValidationForGetWebhook() throws Exception {
    // given
    httpServletRequestImpl.setMethod("GET");
    httpServletRequestImpl.setRequestURI(webhookPath);

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    verify(webhookSecurityService, never())
        .isSignatureValid(validSha256HeaderValue, bodyBytes, webhookSecret);
    verify(filterChain, times(1)).doFilter(httpServletRequestImpl, httpServletResponseImpl);
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("Given POST non-webhook path, when filtering, then forward without validation")
  void forwardsWithoutValidationForPostNonWebhookPath() throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(apiPath);

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    verify(webhookSecurityService, never())
        .isSignatureValid(validSha256HeaderValue, bodyBytes, webhookSecret);
    verify(filterChain, times(1)).doFilter(httpServletRequestImpl, httpServletResponseImpl);
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(200);
  }

  @ParameterizedTest
  @ValueSource(strings = {"sha1=abc", "sha256=zz", "sha256=12"})
  @DisplayName(
      "Given POST /webhook with invalid header {0}, when filtering, then return 401 and stop chain")
  void returns401AndStopsChainForInvalidHeader(String header) throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent(bodyBytes);
    if (header != null) {
      httpServletRequestImpl.addHeader(validSha256HeaderName, header);
      when(webhookSecurityService.isSignatureValid(eq(header), any(), any())).thenReturn(false);
    } else {
      when(webhookSecurityService.isSignatureValid(isNull(), any(), any())).thenReturn(false);
    }

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(401);
    verify(filterChain, never())
        .doFilter(any(HttpServletRequest.class), eq(httpServletResponseImpl));
  }

  @Test
  @DisplayName(
      "Given POST /webhook with isValid signature, when filtering, then forward request and wrap it")
  void forwardsAndWrapsForValidSignature() throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent(bodyBytes);
    httpServletRequestImpl.addHeader(validSha256HeaderName, validSha256HeaderValue);

    when(webhookSecurityService.isSignatureValid(any(), any(), any())).thenReturn(true);

    // when
    webhookSignatureFilterImpl.doFilter(
        httpServletRequestImpl, httpServletResponseImpl, filterChain);

    // then
    var requestCaptor = forClass(HttpServletRequest.class);
    verify(filterChain, times(1)).doFilter(requestCaptor.capture(), eq(httpServletResponseImpl));
    var wrapped = requestCaptor.getValue();
    assertThat(wrapped).isNotSameAs(httpServletRequestImpl);
    assertThat(httpServletResponseImpl.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Given POST /webhook with isValid signature, when filtering, then allow downstream to read body")
  void allowsDownstreamToReadBodyForValidSignature() throws Exception {
    // given
    httpServletRequestImpl.setMethod(postMethod);
    httpServletRequestImpl.setRequestURI(webhookPath);
    httpServletRequestImpl.setContent(bodyJson.getBytes(UTF_8));
    httpServletRequestImpl.addHeader(validSha256HeaderName, validSha256HeaderValue);

    var chain = new BodyReadingChain();

    // when
    webhookSignatureFilterImpl.doFilter(httpServletRequestImpl, httpServletResponseImpl, chain);

    // then
    assertThat(chain.capturedRequest)
        .isInstanceOf(HttpServletRequest.class)
        .isNotSameAs(httpServletRequestImpl);
    assertThat(chain.capturedBody).isEqualTo(bodyJson);
  }

  private static final class BodyReadingChain implements FilterChain {
    ServletRequest capturedRequest;
    String capturedBody;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException {
      this.capturedRequest = request;
      var bytes = request.getInputStream().readAllBytes();
      this.capturedBody = new String(bytes, UTF_8);
    }
  }
}
