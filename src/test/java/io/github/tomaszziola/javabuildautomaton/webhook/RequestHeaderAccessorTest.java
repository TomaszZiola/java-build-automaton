package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.ServletRequestAttributes;

class RequestHeaderAccessorTest extends BaseUnit {

  @Test
  @DisplayName("Given no request attributes, when reading deliveryId, then return null")
  void shouldReturnNull_whenNoRequestAttributes() {
    // when & then
    assertThat(requestHeaderAccessorImpl.deliveryId()).isNull();
  }

  @Test
  @DisplayName("Given request with header, when reading deliveryId, then return value")
  void shouldReturnHeaderValue_whenPresent() {
    httpServletRequestImpl.addHeader("X-GitHub-Delivery", "abc-123");
    setRequestAttributes(new ServletRequestAttributes(httpServletRequestImpl));

    // when & then
    assertThat(requestHeaderAccessorImpl.deliveryId()).isEqualTo("abc-123");
  }
}
