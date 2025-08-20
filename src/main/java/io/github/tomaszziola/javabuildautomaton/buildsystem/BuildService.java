package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.BUILD_FAILED_PREFIX;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.utils.LogUtils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

  private final BuildRepository buildRepository;
  private final GitCommandRunner gitCommandRunner;
  private final BuildExecutor buildExecutor;

  @Autowired
  public BuildService(
      final BuildRepository buildRepository,
      final GitCommandRunner gitCommandRunner,
      final BuildExecutor buildExecutor) {
    this.buildRepository = buildRepository;
    this.gitCommandRunner = gitCommandRunner;
    this.buildExecutor = buildExecutor;
  }

  public void startBuildProcess(final Project project) {
    LOGGER.info("Starting build process for project: {}", project.getName());

    final var build = new Build();
    build.setProject(project);
    build.setStartTime(LocalDateTime.now());
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);

    final var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDir = new File(project.getLocalPath());
    if (!workingDir.exists() || !workingDir.isDirectory()) {
      build.setStatus(FAILED);
      logBuilder.append(BUILD_FAILED_PREFIX + "No such file or directory");
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
      final var pullResult = gitCommandRunner.pull(workingDir);
      final var pullLogs = pullResult.logs();

      logBuilder.append(pullLogs);
      if (!pullResult.isSuccess()) {
        build.setStatus(FAILED);
        LOGGER.error("Git pull failed for project: {}", project.getName());
        return;
      }

      final var buildResult = buildExecutor.build(project.getBuildTool(), workingDir);
      final var buildLogs = buildResult.logs();
      if (!LogUtils.areLogsEquivalent(pullLogs, buildLogs)) {
        logBuilder.append(buildLogs);
      }

      build.setStatus(buildResult.isSuccess() ? SUCCESS : FAILED);
      if (buildResult.isSuccess()) {
        LOGGER.info("Build process finished successfully for project: {}", project.getName());
      } else {
        LOGGER.error(BUILD_FAILED_PREFIX + "during build step for project: {}", project.getName());
      }
    } catch (final InterruptedException e) {
      build.setStatus(FAILED);
      logBuilder.append(BUILD_FAILED_PREFIX).append(e.getMessage());
      LOGGER.error(BUILD_FAILED_PREFIX + "for project: {}", project.getName(), e);
      Thread.currentThread().interrupt();
    } catch (final IOException e) {
      build.setStatus(FAILED);
      logBuilder.append(BUILD_FAILED_PREFIX).append(e.getMessage());
      LOGGER.error(BUILD_FAILED_PREFIX + "for project: {}", project.getName(), e);
    } finally {
      build.setEndTime(LocalDateTime.now());
      build.setLogs(logBuilder.toString());
      buildRepository.save(build);
    }
  }
}
