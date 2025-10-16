package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildExecutor.class);

  private static final String CMD_MVN = "mvn";
  private static final String CMD_GRADLE = "gradle";
  private static final String CMD_GRADLEW = "gradlew";
  private static final String ARG_CLEAN = "clean";
  private static final String ARG_BUILD = "build";
  private static final String ARG_INSTALL = "install";

  private final ProcessExecutor processExecutor;

  public ExecutionResult build(BuildTool buildTool, File workingDir) {
    return switch (buildTool) {
      case MAVEN -> runMaven(workingDir);
      case GRADLE -> runGradle(workingDir);
    };
  }

  private ExecutionResult runMaven(File workingDir) {
    return processExecutor.execute(workingDir, CMD_MVN, ARG_CLEAN, ARG_INSTALL);
  }

  private ExecutionResult runGradle(File workingDir) {
    var gradlew = new File(workingDir, CMD_GRADLEW);
    if (!gradlew.exists() || !gradlew.isFile()) {
      return processExecutor.execute(workingDir, CMD_GRADLE, ARG_CLEAN, ARG_BUILD);
    }

    var executable = gradlew.canExecute();
    if (!executable) {
      executable = gradlew.setExecutable(true);
      if (!executable) {
        LOGGER.warn(
            "Failed to set executable bit on '{}'. Falling back to 'gradle' command.",
            gradlew.getAbsolutePath());
      }
    }

    var cmd = executable ? gradlew.getAbsolutePath() : CMD_GRADLE;
    return processExecutor.execute(workingDir, cmd, ARG_CLEAN, ARG_BUILD);
  }
}
