package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

  private final ProcessRunner processRunner;
  private final OutputCollector outputCollector;

  public ProcessExecutor(final ProcessRunner processRunner, final OutputCollector outputCollector) {
    this.processRunner = processRunner;
    this.outputCollector = outputCollector;
  }

  public ExecutionResult execute(final File workingDir, final String... command) {
    LOGGER.info("Executing command in '{}': {}", workingDir, join(" ", command));

    final var logOutput = new StringBuilder(LOG_INITIAL_CAPACITY);

    try {
      final var process = processRunner.start(workingDir, command);

      outputCollector.collect(process.getInputStream(), logOutput);

      final int exitCode = process.waitFor();
      final boolean isSuccess = exitCode == 0;
      if (!isSuccess) {
        logOutput.append("[[ERROR]] Command failed: ").append(join(" ", command));
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
