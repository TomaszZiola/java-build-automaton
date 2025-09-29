package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.constants.Constants.LOG_INITIAL_CAPACITY;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import jakarta.transaction.Transactional;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

  private final BuildExecutor buildExecutor;
  private final BuildLifecycleService buildLifecycleService;
  private final BuildRepository buildRepository;
  private final GitCommandRunner gitCommandRunner;
  private final WorkingDirectoryValidator workingDirectoryValidator;

  @Autowired
  public BuildService(
      final BuildExecutor buildExecutor,
      final BuildLifecycleService buildLifecycleService,
      final BuildRepository buildRepository,
      final GitCommandRunner gitCommandRunner,
      final WorkingDirectoryValidator workingDirectoryValidator) {
    this.buildExecutor = buildExecutor;
    this.buildLifecycleService = buildLifecycleService;
    this.buildRepository = buildRepository;
    this.gitCommandRunner = gitCommandRunner;
    this.workingDirectoryValidator = workingDirectoryValidator;
  }

  public void startBuildProcess(final Project project) {
    LOGGER.info("Starting build process for project: {}", project.getName());
    final var build = buildLifecycleService.createInProgress(project);
    executeBuildSteps(project, build);
  }

  private void executeBuildSteps(final Project project, final Build build) {
    final var logBuilder = new StringBuilder(LOG_INITIAL_CAPACITY);

    final var workingDirectoryStatus =
        workingDirectoryValidator.prepareWorkspace(project, build, logBuilder);
    if (!workingDirectoryStatus.isValid()) {
      return;
    }
    final var workingDirectory = workingDirectoryStatus.workingDirectory();

    if (!runGitSync(project, build, workingDirectory, logBuilder)) {
      return;
    }
    runProjectBuild(project, build, workingDirectory, logBuilder);
  }

  private boolean runGitSync(
      final Project project,
      final Build build,
      final File workingDirectory,
      final StringBuilder logBuilder) {

    final var repoInitialized = new File(workingDirectory, ".git").isDirectory();
    final var gitResult =
        repoInitialized
            ? gitCommandRunner.pull(workingDirectory)
            : gitCommandRunner.clone(project.getRepositoryUrl(), workingDirectory);

    logBuilder.append(gitResult.logs());
    if (!gitResult.isSuccess()) {
      buildLifecycleService.complete(build, FAILED, logBuilder);
      final var action = repoInitialized ? "pull" : "clone";
      LOGGER.error("Git {} failed for project: {}", action, project.getName());
      return false;
    }
    return true;
  }

  private void runProjectBuild(
      final Project project,
      final Build build,
      final File workingDirectory,
      final StringBuilder logBuilder) {

    final var buildResult = buildExecutor.build(project.getBuildTool(), workingDirectory);
    logBuilder.append(buildResult.logs());
    if (!buildResult.isSuccess()) {
      buildLifecycleService.complete(build, FAILED, logBuilder);
      LOGGER.error("Build failed for project: {}", project.getName());
      return;
    }
    buildLifecycleService.complete(build, SUCCESS, logBuilder);
  }

  @Transactional
  public void executeBuild(final long buildId) {
    final var build =
        buildRepository.findById(buildId).orElseThrow(() -> new BuildNotFoundException(buildId));
    final var project = build.getProject();
    LOGGER.info("Executing build #{} for project: {}", build.getId(), project.getName());
    buildLifecycleService.markInProgress(build);
    executeBuildSteps(project, build);
  }

  public Build createQueuedBuild(final Project project) {
    return buildLifecycleService.createQueued(project);
  }
}
