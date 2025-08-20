package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class BuildExecutor {

  private final ProcessExecutor processExecutor;

  public BuildExecutor(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult build(final BuildTool buildTool, final File workingDir)
      throws IOException, InterruptedException {
    return switch (buildTool) {
      case MAVEN -> processExecutor.execute(workingDir, "mvn", "clean", "install");
      case GRADLE -> processExecutor.execute(workingDir, "gradle", "clean", "build");
    };
  }
}
