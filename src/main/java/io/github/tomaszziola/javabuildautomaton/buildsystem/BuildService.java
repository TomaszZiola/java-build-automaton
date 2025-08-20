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

    final var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDir = new File(project.getLocalPath());
    if (!isWorkingDirExisting(project, workingDir, build, logBuilder)) {
      return;
    }

    if (proceedGit(
        gitCommandRunner.pull(workingDir),
        logBuilder,
        build,
        "Git pull failed for project: {}",
        project)) {
      return;
    }

    if (proceedGit(
        buildExecutor.build(project.getBuildTool(), workingDir),
        logBuilder,
        build,
        "Build failed for project: {}",
        project)) {
      return;
    }

    finishBuild(build, SUCCESS, logBuilder);
    buildRepository.save(build);
  }

  private boolean proceedGit(
      ExecutionResult executionResult,
      StringBuilder logBuilder,
      Build build,
      String errorMessage,
      Project project) {
    final var logs = executionResult.logs();

    logBuilder.append(logs);
    if (!executionResult.isSuccess()) {
      finishBuild(build, FAILED, logBuilder);
      LOGGER.error(errorMessage, project.getName());
      return true;
    }
    return false;
  }

  private void finishBuild(Build build, BuildStatus status, StringBuilder logBuilder) {
    build.setStatus(status);
    build.setLogs(logBuilder.toString());
    build.setEndTime(LocalDateTime.now());
    buildRepository.save(build);
  }

  private boolean isWorkingDirExisting(
      Project project, File workingDir, Build build, StringBuilder logBuilder) {
    if (!workingDir.exists() || !workingDir.isDirectory()) {
      build.setStatus(FAILED);
      logBuilder.append("[[ERROR]]  No such file or directory");
      build.setEndTime(LocalDateTime.now());
      build.setLogs(logBuilder.toString());
      buildRepository.save(build);
      LOGGER.error(
          "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory: {}",
          project.getName(),
          workingDir.getAbsolutePath());
      return false;
    }
    return true;
  }
}
