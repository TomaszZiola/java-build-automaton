package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;
import static java.util.Set.of;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.workspace.BuildWorkspaceGuard;
import jakarta.transaction.Transactional;
import java.io.File;
import java.util.Set;
import java.util.function.BooleanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

  private final Set<Integer> allowedJavaVersions = of(21, 25);

  private final BuildExecutor buildExecutor;
  private final BuildLifecycleService buildLifecycleService;
  private final BuildMapper buildMapper;
  private final BuildRepository buildRepository;
  private final GitCommandRunner gitCommandRunner;
  private final BuildWorkspaceGuard buildWorkspaceGuard;

  @Transactional
  public void execute(long buildId) {
    var build =
        buildRepository.findById(buildId).orElseThrow(() -> new BuildNotFoundException(buildId));
    var project = build.getProject();
    log.info("Executing build #{} for project: {}", build.getId(), project.getRepositoryName());
    buildLifecycleService.markInProgress(build);
    executeBuildPipeline(project, build);
  }

  private void executeBuildPipeline(Project project, Build build) {
    var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    var workingDirectoryStatus =
        buildWorkspaceGuard.prepareWorkspaceOrFail(project, build, logBuilder);
    if (!workingDirectoryStatus.isValid()) {
      failAndLog(
          build,
          logBuilder,
          "Workspace preparation failed for project: {}",
          project.getRepositoryName());
      return;
    }

    var workingDirectory = workingDirectoryStatus.workingDirectory();

    if (failIfFalse(
        () -> synchronizeRepository(project, workingDirectory, logBuilder).isSuccess(),
        () ->
            failAndLog(
                build,
                logBuilder,
                "Git synchronization failed for project: {}",
                project.getRepositoryName()))) {
      return;
    }

    if (failIfFalse(
        () -> executeProjectBuild(project, workingDirectory, logBuilder).isSuccess(),
        () ->
            failAndLog(
                build, logBuilder, "Build failed for project: {}", project.getRepositoryName()))) {
      return;
    }

    completeBuildWithLogs(
        build, SUCCESS, logBuilder, "Build succeeded for project: {}", project.getRepositoryName());
  }

  private boolean failIfFalse(BooleanSupplier step, Runnable onFailure) {
    if (step.getAsBoolean()) {
      return false;
    }
    onFailure.run();
    return true;
  }

  private void failAndLog(Build build, StringBuilder logBuilder, String message, Object arg) {
    completeBuildWithLogs(build, FAILED, logBuilder, message, arg);
  }

  public void startBuildProcess(Project project) {
    log.info("Starting build process for project: {}", project.getRepositoryName());
    var build = buildLifecycleService.makeInProgress(project);
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
      log.error("Git {} failed for project: {}", action, project.getRepositoryName());
      return gitResult;
    }

    return gitResult;
  }

  private ExecutionResult executeProjectBuild(
      Project project, File workingDirectory, StringBuilder logBuilder) {
    var javaVersion = project.getJavaVersion().getVersionNumber();
    if (!allowedJavaVersions.contains(javaVersion)) {
      log.error(
          "Invalid javaVersion for project: {}. Provided: {}",
          project.getRepositoryName(),
          javaVersion);
      return new ExecutionResult(false, "Invalid javaVersion\n");
    }
    var buildResult = buildExecutor.build(project.getBuildTool(), workingDirectory, javaVersion);
    appendLogs(logBuilder, buildResult.logs());
    return buildResult;
  }

  private void completeBuildWithLogs(
      Build build, BuildStatus status, StringBuilder logBuilder, String message, Object arg) {
    buildLifecycleService.complete(build, status, logBuilder);
    log.info(message, arg);
  }

  private void appendLogs(StringBuilder logBuilder, String logs) {
    if (logs != null && !logs.isEmpty()) {
      logBuilder.append(logs);
    }
  }
}
