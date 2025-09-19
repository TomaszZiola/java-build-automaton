package io.github.tomaszziola.javabuildautomaton.webhook;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestHeaderAccessor {
  private static final String DELIVERY_HEADER = "X-GitHub-Delivery";

  public String deliveryId() {
    final var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return null;
    }
    final HttpServletRequest req = attrs.getRequest();
    return req.getHeader(DELIVERY_HEADER);
  }
}
