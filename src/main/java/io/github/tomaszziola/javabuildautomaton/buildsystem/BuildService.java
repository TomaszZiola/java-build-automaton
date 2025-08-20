package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;

import io.github.tomaszziola.javabuildautomaton.exception.BuildProcessException;
import io.github.tomaszziola.javabuildautomaton.project.Project;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);
  private final BuildRepository buildRepository;
  private final ProcessExecutor processExecutor;

  public BuildService(BuildRepository buildRepository, ProcessExecutor processExecutor) {
    this.buildRepository = buildRepository;
    this.processExecutor = processExecutor;
  }

  public void startBuildProcess(final Project project) {
    LOGGER.info("Starting build process for project: {}", project.getName());

    final var build = new Build();
    build.setProject(project);
    build.setStartTime(LocalDateTime.now());
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);

    final var logBuilder = new StringBuilder();

    try {
      final var workingDir = new File(project.getLocalPath());

      logBuilder.append(processExecutor.execute(workingDir, "git", "pull"));
      logBuilder.append(buildWithTool(project.getBuildTool(), workingDir));

      build.setStatus(SUCCESS);
      LOGGER.info("Build process finished successfully for project: {}", project.getName());
    } catch (final IOException | InterruptedException e) {
      build.setStatus(FAILED);
      logBuilder.append("\nBUILD FAILED:\n").append(e.getMessage());
      LOGGER.error("Build process failed for project: {}", project.getName(), e);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    } finally {
      build.setEndTime(LocalDateTime.now());
      build.setLogs(logBuilder.toString());
      buildRepository.save(build);
    }
  }

  private String buildWithTool(final BuildTool buildTool, final File workingDir)
      throws IOException, InterruptedException {
    return switch (buildTool) {
      case MAVEN -> processExecutor.execute(workingDir, "mvn", "clean", "install");
      case GRADLE -> processExecutor.execute(workingDir, "gradle", "clean", "build");
    };
  }

  private void executeCommand(final File workingDir, final String... command)
      throws IOException, InterruptedException {
    LOGGER.info("Executing command in '{}': {}", workingDir, String.join(" ", command));

    final var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);

    final var process = processBuilder.start();

    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info(line);
      }
    }

    final var exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new BuildProcessException("Command execution failed with exit code: " + exitCode, null);
    }
  }
}
