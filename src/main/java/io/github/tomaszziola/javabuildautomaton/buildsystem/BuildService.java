package io.github.tomaszziola.javabuildautomaton.buildsystem;

import io.github.tomaszziola.javabuildautomaton.exception.BuildProcessException;
import io.github.tomaszziola.javabuildautomaton.project.Project;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

  public void startBuildProcess(final Project project) {
    LOGGER.info("Starting build process for project: {}", project.getName());
    try {
      final File workingDir = new File(project.getLocalPath());

      executeCommand(workingDir, "git", "pull");

      buildWithTool(project.getBuildTool(), workingDir);

      LOGGER.info("Build process finished successfully for project: {}", project.getName());

    } catch (final IOException e) {
      LOGGER.error("Build process failed for project: {}", project.getName(), e);
      throw new BuildProcessException("Failed to execute build command", e);
    } catch (final InterruptedException e) {
      LOGGER.error("Build process failed for project: {}", project.getName(), e);
      Thread.currentThread().interrupt();
      throw new BuildProcessException("Failed to execute build command", e);
    }
  }

  private void buildWithTool(final BuildTool buildTool, final File workingDir)
      throws IOException, InterruptedException {
    switch (buildTool) {
      case MAVEN:
        executeCommand(workingDir, "mvn", "clean", "install");
        break;
      case GRADLE:
        executeCommand(workingDir, "gradle", "clean", "build");
        break;
    }
  }

  private void executeCommand(final File workingDir, final String... command)
      throws IOException, InterruptedException {
    LOGGER.info("Executing command in '{}': {}", workingDir, String.join(" ", command));

    final var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);

    final Process process = processBuilder.start();

    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info(line);
      }
    }

    final int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new BuildProcessException("Command execution failed with exit code: " + exitCode, null);
    }
  }
}
