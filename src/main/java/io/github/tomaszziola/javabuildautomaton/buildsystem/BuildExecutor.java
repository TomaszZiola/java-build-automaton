package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildExecutor {

  private static final String CMD_MVN = "mvn";
  private static final String CMD_GRADLE = "gradle";
  private static final String CMD_GRADLEW = "gradlew";
  private static final String ARG_CLEAN = "clean";
  private static final String ARG_BUILD = "build";
  private static final String ARG_INSTALL = "install";
  private static final String DEFAULT_JDK_PREFIX = "/opt/jdks/jdk-";

  private final ProcessExecutor processExecutor;

  public ExecutionResult build(BuildTool buildTool, File workingDir, int javaVersion) {
    var javaHome = DEFAULT_JDK_PREFIX + javaVersion;
    return switch (buildTool) {
      case MAVEN -> runMaven(workingDir);
      case GRADLE -> runGradle(workingDir, javaHome);
    };
  }

  private ExecutionResult runMaven(File workingDir) {
    return processExecutor.execute(workingDir, CMD_MVN, ARG_CLEAN, ARG_INSTALL);
  }

  private ExecutionResult runGradle(File workingDir, String javaHome) {
    var gradlew = new File(workingDir, CMD_GRADLEW);
    if (!gradlew.exists() || !gradlew.isFile()) {
      return processExecutor.execute(workingDir, CMD_GRADLE, ARG_CLEAN, ARG_BUILD);
    }

    var executable = gradlew.canExecute();
    if (!executable) {
      executable = gradlew.setExecutable(true);
      if (!executable) {
        log.warn(
            "Failed to set executable bit on '{}'. Falling back to 'gradle' command.",
            gradlew.getAbsolutePath());
      }
    }
    var cmd = executable ? gradlew.getAbsolutePath() : CMD_GRADLE;
    var shell =
        "export JAVA_HOME=\""
            + javaHome
            + "\" && export PATH=\"$JAVA_HOME/bin:$PATH\" && \""
            + cmd
            + "\" clean build --stacktrace";
    return processExecutor.execute(workingDir, "sh", "-c", shell);
  }
}
