package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

/** Executes git commands via the local CLI. */
@Service
public class GitCommandRunner {

  private final ProcessExecutor processExecutor;

  public GitCommandRunner(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult pull(final File workingDir) throws IOException, InterruptedException {
    return processExecutor.execute(workingDir, "git", "pull");
  }
}
