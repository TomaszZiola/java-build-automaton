package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.QUEUED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;
import static java.time.Instant.now;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

  private final BuildExecutor buildExecutor;
  private final BuildRepository buildRepository;
  private final GitCommandRunner gitCommandRunner;

  @Autowired
  public BuildService(
      final BuildExecutor buildExecutor,
      final BuildRepository buildRepository,
      final GitCommandRunner gitCommandRunner) {
    this.buildExecutor = buildExecutor;
    this.buildRepository = buildRepository;
    this.gitCommandRunner = gitCommandRunner;
  }

  public void startBuildProcess(final Project project) {
    LOGGER.info("Starting build process for project: {}", project.getName());
    final var build = new Build();
    build.setProject(project);
    build.setStartTime(now());
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);
    executeBuildSteps(project, build);
  }

  private void executeBuildSteps(final Project project, final Build build) {
    final var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDirectory = new File(project.getLocalPath());
    if (!validateWorkingDirectoryExists(project, workingDirectory, build, logBuilder)) {
      return;
    }

    final var pullResult = gitCommandRunner.pull(workingDirectory);
    logBuilder.append(pullResult.logs());
    if (!pullResult.isSuccess()) {
      completeBuild(build, FAILED, logBuilder);
      LOGGER.error("Git pull failed for project: {}", project.getName());
      return;
    }

    final var buildResult = buildExecutor.build(project.getBuildTool(), workingDirectory);
    logBuilder.append(buildResult.logs());
    if (!buildResult.isSuccess()) {
      completeBuild(build, FAILED, logBuilder);
      LOGGER.error("Build failed for project: {}", project.getName());
      return;
    }
    completeBuild(build, SUCCESS, logBuilder);
  }

  public void executeBuild(final long buildId) {
    final var build =
        buildRepository
            .findById(buildId)
            .orElseThrow(() -> new IllegalStateException("Build not found: " + buildId));
    final var project = build.getProject();
    LOGGER.info("Executing build #{} for project: {}", build.getId(), project.getName());
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);
    executeBuildSteps(project, build);
  }

  public Build createQueuedBuild(final Project project) {
    final var build = new Build();
    build.setProject(project);
    build.setStatus(QUEUED);
    build.setStartTime(now());
    return buildRepository.save(build);
  }

  private void completeBuild(
      final Build build, final BuildStatus status, final StringBuilder buildLog) {
    build.setStatus(status);
    build.setLogs(buildLog.toString());
    build.setEndTime(now());
    buildRepository.save(build);
  }

  private boolean validateWorkingDirectoryExists(
      final Project project,
      final File workingDirectory,
      final Build build,
      final StringBuilder buildLog) {
    if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
      buildLog.append("\nBUILD FAILED:\nNo such file or directory");
      completeBuild(build, FAILED, buildLog);
      LOGGER.error(
          "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory: {}",
          project.getName(),
          workingDirectory.getAbsolutePath());
      return false;
    }
    return true;
  }
}
