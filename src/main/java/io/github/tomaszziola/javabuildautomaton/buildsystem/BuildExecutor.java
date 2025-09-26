package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class BuildExecutor {

  private final ProcessExecutor processExecutor;

  public BuildExecutor(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult build(final BuildTool buildTool, final File workingDir) {
    return switch (buildTool) {
      case MAVEN -> processExecutor.execute(workingDir, "mvn", "clean", "install");
      case GRADLE -> runGradle(workingDir);
    };
  }

  private ExecutionResult runGradle(final File workingDir) {
    final File gradlew = new File(workingDir, "gradlew");
    if (gradlew.exists()) {
      if (!gradlew.canExecute()) {
        final boolean madeExecutable = gradlew.setExecutable(true);
        if (!madeExecutable && !gradlew.canExecute()) {
          return processExecutor.execute(workingDir, "gradle", "clean", "build");
        }
      }
      final String gradlewCmd = gradlew.getPath();
      return processExecutor.execute(workingDir, gradlewCmd, "clean", "build");
    }
    return processExecutor.execute(workingDir, "gradle", "clean", "build");
  }
}
