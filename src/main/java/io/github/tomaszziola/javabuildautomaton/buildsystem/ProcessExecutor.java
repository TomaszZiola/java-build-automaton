package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

  public ExecutionResult execute(final File workingDir, final String... command) {
    LOGGER.info("Executing command in '{}': {}", workingDir, join(" ", command));

    final var logOutput = new StringBuilder(LOG_INITIAL_CAPACITY);

    try {
      final var processBuilder = new ProcessBuilder(command);
      processBuilder.directory(workingDir);
      processBuilder.redirectErrorStream(true);

      final var process = processBuilder.start();

      try (var reader =
          new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          logOutput.append(line).append(lineSeparator());
        }
      }
      final int exitCode = process.waitFor();
      final boolean isSuccess = exitCode == 0;
      if (!isSuccess) {
        logOutput
            .append("[[ERROR]] Command failed: ")
            .append(join(" ", command))
            .append(System.lineSeparator());
      }
      return new ExecutionResult(isSuccess, logOutput.toString());
    } catch (IOException e) {
      LOGGER.error("Process execution failed with IOException", e);
      logOutput.append("[[ERROR]] IO failure: ").append(e.getMessage()).append(lineSeparator());
      return new ExecutionResult(false, logOutput.toString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Process execution interrupted", e);
      logOutput.append("[[ERROR]] Interrupted: ").append(e.getMessage()).append(lineSeparator());
      return new ExecutionResult(false, logOutput.toString());
    }
  }
}
