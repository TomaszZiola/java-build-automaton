package io.github.tomaszziola.javabuildautomaton.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FilterOrdersTest {

  @Test
  @DisplayName("Filter orders maintain consistent relative ordering")
  void filterOrdersRelative() {
    assertThat(FilterOrders.WEBHOOK_SIGNATURE - FilterOrders.CORRELATION_ID).isEqualTo(100);
  }
}
