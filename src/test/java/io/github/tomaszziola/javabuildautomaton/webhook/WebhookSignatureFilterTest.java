package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookSignatureFilterTest extends BaseUnit {

  @Test
  @DisplayName("Non-webhook requests are forwarded without validation")
  void nonWebhookBypassesValidation() throws Exception {
    // given
    request.setMethod("GET");
    request.setRequestURI("/api/projects");

    // when
    webhookSignatureFilterImpl.doFilter(request, response, filterChain);

    // then
    verify(webhookSecurityService, never()).isSignatureValid(secret, body);
    verify(filterChain, times(1)).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("POST /webhook with invalid signature returns 401 and stops chain")
  void invalidSignatureReturns401() throws Exception {
    // given
    request.setMethod("POST");
    request.setRequestURI("/webhook");
    request.setContent("{\"hello\":\"world\"}".getBytes(UTF_8));
    request.addHeader("X-Hub-Signature-256", invalidSha256Header);

    when(webhookSecurityService.isSignatureValid(secret, body)).thenReturn(false);

    // when
    webhookSignatureFilterImpl.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("POST /webhook with valid signature forwards request")
  void validSignatureForwards() throws Exception {
    // given
    request.setMethod("POST");
    request.setRequestURI("/webhook");
    request.setContent("{\"msg\":\"hi\"}".getBytes(UTF_8));
    request.addHeader(
        "X-Hub-Signature-256", validSha256Header);

    when(webhookSecurityService.isSignatureValid(eq(validSha256Header), any()))
        .thenReturn(true);

    //when
    webhookSignatureFilterImpl.doFilter(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    assertThat(response.getStatus()).isEqualTo(200);
  }
}
