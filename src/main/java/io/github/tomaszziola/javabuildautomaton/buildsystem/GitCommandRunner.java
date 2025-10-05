package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitCommandRunner {

  private static final String GIT = "git";
  private static final String ARG_PULL = "pull";
  private static final String ARG_CLONE = "clone";
  private static final String ARG_DOTE = ".";

  private final ProcessExecutor processExecutor;

  public ExecutionResult pull(final File workingDir) {
    return processExecutor.execute(workingDir, GIT, ARG_PULL);
  }

  public ExecutionResult clone(final String repositoryUrl, final File targetDir) {
    return processExecutor.execute(targetDir, GIT, ARG_CLONE, repositoryUrl, ARG_DOTE);
  }
}
