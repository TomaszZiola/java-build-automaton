package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.lang.Thread.currentThread;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessExecutor {

  private final ProcessRunner processRunner;
  private final OutputCollector outputCollector;

  public ExecutionResult execute(File workingDir, String... command) {
    log.info("Executing command in '{}': {}", workingDir, join(" ", command));

    var logOutput = new StringBuilder(LOG_INITIAL_CAPACITY);

    try {
      var process = processRunner.start(workingDir, command);

      outputCollector.collect(process.getInputStream(), logOutput);

      var exitCode = process.waitFor();
      var isSuccess = exitCode == 0;
      if (!isSuccess) {
        logOutput.append("[[ERROR]] Command failed: ").append(join(" ", command));
      }

      return new ExecutionResult(isSuccess, logOutput.toString());
    } catch (IOException e) {
      log.error("Process execution failed with IOException", e);
      logOutput.append("[[ERROR]] IO failure: ").append(e.getMessage()).append(lineSeparator());
      return new ExecutionResult(false, logOutput.toString());
    } catch (InterruptedException e) {
      currentThread().interrupt();
      log.error("Process execution interrupted", e);
      logOutput.append("[[ERROR]] Interrupted: ").append(e.getMessage()).append(lineSeparator());
      return new ExecutionResult(false, logOutput.toString());
    }
  }
}
