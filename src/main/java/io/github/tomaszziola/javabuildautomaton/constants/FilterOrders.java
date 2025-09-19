package io.github.tomaszziola.javabuildautomaton.constants;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

public final class FilterOrders {

  public static final int CORRELATION_ID = HIGHEST_PRECEDENCE + 100;
  public static final int WEBHOOK_SIGNATURE = HIGHEST_PRECEDENCE + 200;

  private FilterOrders() {}
}
