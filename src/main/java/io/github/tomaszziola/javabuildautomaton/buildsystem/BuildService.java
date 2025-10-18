package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import jakarta.transaction.Transactional;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

  private final BuildExecutor buildExecutor;
  private final BuildLifecycleService buildLifecycleService;
  private final BuildMapper buildMapper;
  private final BuildRepository buildRepository;
  private final GitCommandRunner gitCommandRunner;
  private final WorkingDirectoryValidator workingDirectoryValidator;

  @Transactional
  public void execute(long buildId) {
    var build =
        buildRepository.findById(buildId).orElseThrow(() -> new BuildNotFoundException(buildId));
    var project = build.getProject();
    LOGGER.info("Executing build #{} for project: {}", build.getId(), project.getRepositoryName());
    buildLifecycleService.markInProgress(build);
    executeBuildPipeline(project, build);
  }

  private void executeBuildPipeline(Project project, Build build) {
    var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDirectoryStatus =
        workingDirectoryValidator.validateAndPrepare(project, build, logBuilder);
    if (!workingDirectoryStatus.isValid()) {
      completeBuildWithLogs(
          build,
          FAILED,
          logBuilder,
          "Workspace preparation failed for project: {}",
          project.getRepositoryName());
      return;
    }

    var workingDirectory = workingDirectoryStatus.workingDirectory();

    var gitResult = synchronizeRepository(project, workingDirectory, logBuilder);
    if (!gitResult.isSuccess()) {
      completeBuildWithLogs(
          build,
          FAILED,
          logBuilder,
          "Git synchronization failed for project: {}",
          project.getRepositoryName());
      return;
    }

    var buildResult = executeProjectBuild(project, workingDirectory, logBuilder);
    if (!buildResult.isSuccess()) {
      completeBuildWithLogs(
          build, FAILED, logBuilder, "Build failed for project: {}", project.getRepositoryName());
      return;
    }

    completeBuildWithLogs(
        build, SUCCESS, logBuilder, "Build succeeded for project: {}", project.getRepositoryName());
  }

  public void startBuildProcess(Project project) {
    LOGGER.info("Starting build process for project: {}", project.getRepositoryName());
    var build = buildLifecycleService.createInProgress(project);
    executeBuildPipeline(project, build);
  }

  public BuildDetailsDto findBuildDetailsById(Long buildId) {
    return buildRepository
        .findById(buildId)
        .map(buildMapper::toDetailsDto)
        .orElseThrow(() -> new BuildNotFoundException(buildId));
  }

  private ExecutionResult synchronizeRepository(
      Project project, File workingDirectory, StringBuilder logBuilder) {
    var repoInitialized = new File(workingDirectory, ".git").isDirectory();
    var gitResult =
        repoInitialized
            ? gitCommandRunner.pull(workingDirectory)
            : gitCommandRunner.clone(project.getRepositoryUrl(), workingDirectory);

    appendLogs(logBuilder, gitResult.logs());

    if (!gitResult.isSuccess()) {
      var action = repoInitialized ? "pull" : "clone";
      LOGGER.error("Git {} failed for project: {}", action, project.getRepositoryName());
      return gitResult;
    }

    return gitResult;
  }

  private ExecutionResult executeProjectBuild(
      Project project, File workingDirectory, StringBuilder logBuilder) {
    var buildResult = buildExecutor.build(project.getBuildTool(), workingDirectory);
    appendLogs(logBuilder, buildResult.logs());
    return buildResult;
  }

  private void completeBuildWithLogs(
      Build build, BuildStatus status, StringBuilder logBuilder, String message, Object arg) {
    buildLifecycleService.complete(build, status, logBuilder);
    LOGGER.info(message, arg);
  }

  private void appendLogs(StringBuilder logBuilder, String logs) {
    if (logs != null && !logs.isEmpty()) {
      logBuilder.append(logs);
    }
  }
}
