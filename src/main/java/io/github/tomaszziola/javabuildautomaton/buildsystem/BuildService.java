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
  public void executeBuild(long buildId) {
    var build =
        buildRepository.findById(buildId).orElseThrow(() -> new BuildNotFoundException(buildId));
    var project = build.getProject();
    LOGGER.info("Executing build #{} for project: {}", build.getId(), project.getRepositoryName());
    buildLifecycleService.markInProgress(build);
    executeBuildSteps(project, build);
  }

  public void startBuildProcess(Project project) {
    LOGGER.info("Starting build process for project: {}", project.getUsername());
    var build = buildLifecycleService.createInProgress(project);
    executeBuildSteps(project, build);
  }

  public BuildDetailsDto findBuildDetailsById(Long buildId) {
    return buildRepository
        .findById(buildId)
        .map(buildMapper::toDetailsDto)
        .orElseThrow(() -> new BuildNotFoundException(buildId));
  }

  private void executeBuildSteps(Project project, Build build) {
    var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    var workingDirectoryStatus =
        workingDirectoryValidator.prepareWorkspace(project, build, logBuilder);
    if (!workingDirectoryStatus.isValid()) {
      return;
    }
    var workingDirectory = workingDirectoryStatus.workingDirectory();

    var gitSyncStatus = runGitSync(project, build, workingDirectory, logBuilder);
    if (!gitSyncStatus.isSuccess()) {
      return;
    }
    runProjectBuild(project, build, workingDirectory, logBuilder);
  }

  private ExecutionResult runGitSync(
      Project project, Build build, File workingDirectory, StringBuilder logBuilder) {

    var repoInitialized = new File(workingDirectory, ".git").isDirectory();
    var gitResult =
        repoInitialized
            ? gitCommandRunner.pull(workingDirectory)
            : gitCommandRunner.clone(project.getRepositoryUrl(), workingDirectory);

    logBuilder.append(gitResult.logs());
    if (!gitResult.isSuccess()) {
      buildLifecycleService.complete(build, FAILED, logBuilder);
      var action = repoInitialized ? "pull" : "clone";
      LOGGER.error("Git {} failed for project: {}", action, project.getUsername());
      return gitResult;
    }
    return gitResult;
  }

  private void runProjectBuild(
      Project project, Build build, File workingDirectory, StringBuilder logBuilder) {

    var buildResult = buildExecutor.build(project.getBuildTool(), workingDirectory);
    logBuilder.append(buildResult.logs());
    if (!buildResult.isSuccess()) {
      buildLifecycleService.complete(build, FAILED, logBuilder);
      LOGGER.error("Build failed for project: {}", project.getUsername());
      return;
    }
    buildLifecycleService.complete(build, SUCCESS, logBuilder);
  }
}
