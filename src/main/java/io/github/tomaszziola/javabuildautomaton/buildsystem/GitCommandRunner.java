package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class GitCommandRunner {

  private final ProcessExecutor processExecutor;

  public GitCommandRunner(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult pull(final File workingDir) {
    return processExecutor.execute(workingDir, "git", "pull");
  }

  public ExecutionResult cloneRepo(final String repositoryUrl, final File targetDir) {
    return processExecutor.execute(targetDir, "git", "clone", repositoryUrl, ".");
  }
}
