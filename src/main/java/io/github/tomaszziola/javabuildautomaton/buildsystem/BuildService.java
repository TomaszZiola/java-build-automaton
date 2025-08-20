package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import java.io.File;
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

    final var buildLog = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDirectory = new File(project.getLocalPath());
    if (!validateWorkingDirectoryExists(project, workingDirectory, build, buildLog)) {
      return;
    }

    if (failIfUnsuccessful(
        gitCommandRunner.pull(workingDirectory),
        buildLog,
        build,
        "Git pull failed for project: {}",
        project)) {
      return;
    }

    if (failIfUnsuccessful(
        buildExecutor.build(project.getBuildTool(), workingDirectory),
        buildLog,
        build,
        "Build failed for project: {}",
        project)) {
      return;
    }

    completeBuild(build, SUCCESS, buildLog);
  }

  private boolean failIfUnsuccessful(
      final ExecutionResult result,
      final StringBuilder buildLog,
      final Build build,
      final String failureMessage,
      final Project project) {
    final var logs = result.logs();

    buildLog.setLength(0);
    buildLog.append(logs);
    if (!result.isSuccess()) {
      completeBuild(build, FAILED, buildLog);
      LOGGER.error(failureMessage, project.getName());
      return true;
    }
    return false;
  }

  private void completeBuild(
      final Build build, final BuildStatus status, final StringBuilder buildLog) {
    build.setStatus(status);
    build.setLogs(buildLog.toString());
    build.setEndTime(LocalDateTime.now());
    buildRepository.save(build);
  }

  private boolean validateWorkingDirectoryExists(
      final Project project,
      final File workingDirectory,
      final Build build,
      final StringBuilder buildLog) {
    if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
      buildLog.setLength(0);
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
