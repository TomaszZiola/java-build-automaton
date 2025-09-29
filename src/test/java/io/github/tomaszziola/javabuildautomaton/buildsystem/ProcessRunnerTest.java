package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcessRunnerTest extends BaseUnit {

  @Test
  @DisplayName("start merges stderr into stdout (e.g., 'java -version' outputs to stderr)")
  void startMergesErrorStreamIntoStdout() throws Exception {
    // when
    final var process = processRunnerImpl.start(workingDir, "java", "-version");

    // then
    final boolean finished = process.waitFor(10, TimeUnit.SECONDS);
    assertThat(finished).as("process should finish within timeout").isTrue();
    final var bytes = process.getInputStream().readAllBytes();
    final var output = new String(bytes, StandardCharsets.UTF_8);
    assertThat(output.toLowerCase(ROOT)).contains("version");
  }

  @Test
  @DisplayName("start throws IOException when executable is not found")
  void startThrowsIOExceptionWhenCommandNotFound() {
    // when / then
    assertThrows(
        IOException.class,
        () -> processRunnerImpl.start(workingDir, "definitely-not-existing-XYZ-12345"));
  }
}
