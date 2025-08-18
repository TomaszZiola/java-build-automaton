package io.github.tomaszziola.javabuildautomaton.build;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class BuildServiceTest extends BaseUnit {

  @Test
  void givenProject_whenStartBuildProcess_thenCompletesSuccessfully() {
    // when && then
    assertDoesNotThrow(() -> buildService.startBuildProcess(project));
  }
}
