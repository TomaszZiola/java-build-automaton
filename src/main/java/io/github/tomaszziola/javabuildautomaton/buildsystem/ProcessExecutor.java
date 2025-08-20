package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.lang.Math.max;
import static java.lang.String.join;
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

  public ExecutionResult execute(final File workingDir, final String... command)
      throws IOException, InterruptedException {
    LOGGER.info("Executing command in '{}': {}", workingDir, join(" ", command));

    final int initialCapacity = max(256, join(" ", command).length() + 128);
    final var logOutput = new StringBuilder(initialCapacity);

    final var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);

    final var process = processBuilder.start();

    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logOutput.append(line).append(System.lineSeparator());
      }
    }
    final int exitCode = process.waitFor();
    final boolean isSuccess = exitCode == 0;

    if (!isSuccess) {
      logOutput.append("\nCommand failed with exit code: ").append(exitCode);
    }
    return new ExecutionResult(isSuccess, logOutput.toString());
  }
}
