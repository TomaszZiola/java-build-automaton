package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class GitCommandRunner {

  private static final String GIT = "git";
  private static final String ARG_PULL = "pull";
  private static final String ARG_CLONE = "clone";
  private static final String ARG_DOTE = ".";

  private final ProcessExecutor processExecutor;

  public GitCommandRunner(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult pull(final File workingDir) {
    return processExecutor.execute(workingDir, GIT, ARG_PULL);
  }

  public ExecutionResult cloneRepo(final String repositoryUrl, final File targetDir) {
    return processExecutor.execute(targetDir, GIT, ARG_CLONE, repositoryUrl, ARG_DOTE);
  }
}
