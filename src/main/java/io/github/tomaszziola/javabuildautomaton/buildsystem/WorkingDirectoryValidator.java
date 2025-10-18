package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingDirectoryValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkingDirectoryValidator.class);
  private static final String GENERIC_FAILURE_LOG = "\nBUILD FAILED:\nNo such file or directory";
  private static final String LOG_ERR_PREFIX =
      "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory";
  private static final String LOG_ERR_WITH_PATH = LOG_ERR_PREFIX + ": {}";

  private final BuildLifecycleService buildLifecycleService;
  private final WorkspaceService workspaceService;

  public ValidationResult validateAndPrepare(Project project, Build build, StringBuilder buildLog) {
    File workingDirectory;
    try {
      var projectWorkspace = workspaceService.ensureWorkspaceFor(project);
      workingDirectory = projectWorkspace.toFile();
    } catch (WorkspaceException ex) {
      failBuildDueToInvalidWorkspace(project, build, buildLog, null);
      return new ValidationResult(false, null);
    }
    return validateDirectory(workingDirectory, project, build, buildLog);
  }

  private ValidationResult validateDirectory(
      File dir, Project project, Build build, StringBuilder buildLog) {
    if (dir == null || !dir.exists() || !dir.isDirectory()) {
      failBuildDueToInvalidWorkspace(
          project, build, buildLog, dir == null ? null : dir.getAbsolutePath());
      return new ValidationResult(false, null);
    }
    return new ValidationResult(true, dir);
  }

  private void failBuildDueToInvalidWorkspace(
      Project project, Build build, CharSequence buildLog, String pathText) {
    appendGenericFailure(buildLog);
    buildLifecycleService.complete(build, FAILED, buildLog);
    if (pathText != null) {
      logWorkspaceError(project, pathText);
      return;
    }
    logWorkspaceError(project, resolveWorkspacePathText(project));
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
      LOGGER.error(LOG_ERR_WITH_PATH, project.getUsername(), pathText);
    } else {
      LOGGER.error(LOG_ERR_PREFIX, project.getUsername());
    }
  }

  private String resolveWorkspacePathText(Project project) {
    try {
      var workspacePath = workspaceService.resolveWorkspacePath(project);
      return workspacePath.toAbsolutePath().toString();
    } catch (WorkspaceException inner) {
      return null;
    }
  }
}
