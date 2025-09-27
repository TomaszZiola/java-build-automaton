package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class BuildExecutor {

  private static final String CMD_MVN = "mvn";
  private static final String CMD_GRADLE = "gradle";
  private static final String CMD_GRADLEW = "gradlew";
  private static final String ARG_CLEAN = "clean";
  private static final String ARG_BUILD = "build";
  private static final String ARG_INSTALL = "install";

  private final ProcessExecutor processExecutor;

  public BuildExecutor(final ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  public ExecutionResult build(final BuildTool buildTool, final File workingDir) {
    return switch (buildTool) {
      case MAVEN -> processExecutor.execute(workingDir, CMD_MVN, ARG_CLEAN, ARG_INSTALL);
      case GRADLE -> runGradle(workingDir);
    };
  }

  private ExecutionResult runGradle(final File workingDir) {
    final File gradlew = new File(workingDir, CMD_GRADLEW);
    if (gradlew.exists()) {
      if (!gradlew.canExecute()) {
        gradlew.setExecutable(true);
      }
      final String cmd = gradlew.canExecute() ? gradlew.getPath() : CMD_GRADLE;
      return processExecutor.execute(workingDir, cmd, ARG_CLEAN, ARG_BUILD);
    }
    return processExecutor.execute(workingDir, CMD_GRADLE, ARG_CLEAN, ARG_BUILD);
  }
}
