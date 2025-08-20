package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);
  private final BuildRepository buildRepository;
  private final ProcessExecutor processExecutor;

  public BuildService(
      final BuildRepository buildRepository, final ProcessExecutor processExecutor) {
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

    final var logBuilder = new StringBuilder(256);

    final var workingDir = new File(project.getLocalPath());
    if (!workingDir.exists() || !workingDir.isDirectory()) {
      build.setStatus(FAILED);
      logBuilder.append("\nBUILD FAILED:\nNo such file or directory");
      build.setEndTime(LocalDateTime.now());
      build.setLogs(logBuilder.toString());
      buildRepository.save(build);
      LOGGER.error(
          "Build process failed for project: {} - working directory does not exist or is not a directory: {}",
          project.getName(),
          workingDir.getAbsolutePath());
      return;
    }

    try {
      final var pullLogs = processExecutor.execute(workingDir, "git", "pull").logs();
      final var buildLogs = buildWithTool(project.getBuildTool(), workingDir).logs();

      logBuilder.append(pullLogs);
      if (!pullLogs.equals(buildLogs)) {
        logBuilder.append(buildLogs);
      }

      build.setStatus(SUCCESS);
      LOGGER.info("Build process finished successfully for project: {}", project.getName());
    } catch (final InterruptedException e) {
      build.setStatus(FAILED);
      logBuilder.append("\nBUILD FAILED:\n").append(e.getMessage());
      LOGGER.error("Build process failed for project: {}", project.getName(), e);
      Thread.currentThread().interrupt();
    } catch (final IOException e) {
      build.setStatus(FAILED);
      logBuilder.append("\nBUILD FAILED:\n").append(e.getMessage());
      LOGGER.error("Build process failed for project: {}", project.getName(), e);
    } finally {
      build.setEndTime(LocalDateTime.now());
      build.setLogs(logBuilder.toString());
      buildRepository.save(build);
    }
  }

  private ExecutionResult buildWithTool(final BuildTool buildTool, final File workingDir)
      throws IOException, InterruptedException {
    return switch (buildTool) {
      case MAVEN -> processExecutor.execute(workingDir, "mvn", "clean", "install");
      case GRADLE -> processExecutor.execute(workingDir, "gradle", "clean", "build");
    };
  }
}
