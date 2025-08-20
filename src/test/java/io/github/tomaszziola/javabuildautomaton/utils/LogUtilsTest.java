package io.github.tomaszziola.javabuildautomaton.utils;

import static io.github.tomaszziola.javabuildautomaton.utils.LogUtils.areLogsEquivalent;
import static io.github.tomaszziola.javabuildautomaton.utils.LogUtils.normalize;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LogUtilsTest extends BaseUnit {

  @Test
  void givenStringWithMixedLineEndingsAndWhitespace_whenNormalize_thenConvertsAndStrips() {
    // when
    final String normalized = normalize(input);

    // then
    assertThat(normalized).isEqualTo("Hello\nWorld");
  }

  @Test
  void givenVariousInputs_whenAreLogsEquivalent_thenHandlesNullsNormalizationAndContainment() {
    assertThat(areLogsEquivalent(null, null)).isTrue();
    assertThat(areLogsEquivalent(null, "x")).isFalse();
    assertThat(areLogsEquivalent("x", null)).isFalse();

    assertThat(areLogsEquivalent(" line\r\n", "line\n")).isTrue();

    assertThat(areLogsEquivalent("abc\nxyz", "abc")).isTrue();
    assertThat(areLogsEquivalent("build ok\n", "git pulled\nbuild ok\n")).isTrue();

    assertThat(areLogsEquivalent("foo", "bar")).isFalse();
  }
}
