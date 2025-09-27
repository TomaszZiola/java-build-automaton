package io.github.tomaszziola.javabuildautomaton.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstantsTest {

  @Test
  @DisplayName("LOG_INITIAL_CAPACITY is set to expected value and positive")
  void logInitialCapacityConstant() {
    assertThat(Constants.LOG_INITIAL_CAPACITY).isEqualTo(1024);
    assertThat(Constants.LOG_INITIAL_CAPACITY).isPositive();
  }
}
