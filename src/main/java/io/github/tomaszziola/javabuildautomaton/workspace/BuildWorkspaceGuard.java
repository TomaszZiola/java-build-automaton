package io.github.tomaszziola.javabuildautomaton.workspace;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildLifecycleService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ValidationResult;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildWorkspaceGuard {

  private static final String GENERIC_FAILURE_LOG = "\nBUILD FAILED:\nNo such file or directory";
  private static final String LOG_ERR_PREFIX =
      "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory";
  private static final String LOG_ERR_WITH_PATH = LOG_ERR_PREFIX + ": {}";

  private final BuildLifecycleService buildLifecycleService;
  private final WorkspaceManager workspaceManager;

  public ValidationResult prepareWorkspaceOrFail(
      Project project, Build build, StringBuilder buildLog) {
    File workingDirectory;
    try {
      var projectWorkspace = workspaceManager.ensureWorkspaceFor(project);
      workingDirectory = projectWorkspace.toFile();
    } catch (WorkspaceException _) {
      failBuildForWorkspaceError(project, build, buildLog, null);
      return new ValidationResult(false, null);
    }
    return validateWorkspaceDir(workingDirectory, project, build, buildLog);
  }

  private ValidationResult validateWorkspaceDir(
      File dir, Project project, Build build, StringBuilder buildLog) {
    if (dir == null || !dir.exists() || !dir.isDirectory()) {
      failBuildForWorkspaceError(
          project, build, buildLog, dir == null ? null : dir.getAbsolutePath());
      return new ValidationResult(false, null);
    }
    return new ValidationResult(true, dir);
  }

  private void failBuildForWorkspaceError(
      Project project, Build build, CharSequence buildLog, String pathText) {
    appendGenericFailure(buildLog);
    buildLifecycleService.complete(build, FAILED, buildLog);
    if (pathText != null) {
      logWorkspaceError(project, pathText);
      return;
    }
    logWorkspaceError(project, resolveWorkspacePathString(project));
  }

  private void appendGenericFailure(CharSequence buildLog) {
    if (buildLog instanceof StringBuilder sb) {
      sb.append(GENERIC_FAILURE_LOG);
    } else {
      new StringBuilder(buildLog).append(GENERIC_FAILURE_LOG);
    }
  }

  private void logWorkspaceError(Project project, String pathText) {
    if (pathText != null) {
      log.error(LOG_ERR_WITH_PATH, project.getUsername(), pathText);
    } else {
      log.error(LOG_ERR_PREFIX, project.getUsername());
    }
  }

  private String resolveWorkspacePathString(Project project) {
    try {
      var workspacePath = workspaceManager.resolveProjectWorkspacePath(project);
      return workspacePath.toAbsolutePath().toString();
    } catch (WorkspaceException _) {
      return null;
    }
  }
}
