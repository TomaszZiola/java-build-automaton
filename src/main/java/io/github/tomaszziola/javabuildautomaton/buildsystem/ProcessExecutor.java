package io.github.tomaszziola.javabuildautomaton.buildsystem;

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

  public String execute(final File workingDir, final String... command)
      throws IOException, InterruptedException {
    LOGGER.info("Executing command in '{}': {}", workingDir, String.join(" ", command));

    final var logOutput = new StringBuilder();

    final var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);

    final var process = processBuilder.start();

    try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logOutput.append(line).append(System.lineSeparator());
      }
    }
    final int exitCode = process.waitFor();
    if (exitCode != 0) {
      logOutput.append("\nCommand failed with exit code: ").append(exitCode);
      throw new IOException("Command execution failed with exit code: " + exitCode);
    }
    return logOutput.toString();
  }
}
