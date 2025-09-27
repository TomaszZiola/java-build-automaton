package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkingDirectoryValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkingDirectoryValidator.class);

  private final BuildLifecycleService buildLifecycleService;
  private final WorkspaceService workspaceService;

  @Autowired
  public WorkingDirectoryValidator(
      final WorkspaceService workspaceService, final BuildLifecycleService buildLifecycleService) {
    this.workspaceService = workspaceService;
    this.buildLifecycleService = buildLifecycleService;
  }

  public ValidationResult prepareWorkspace(
      final Project project, final Build build, final StringBuilder buildLog) {
    final File workingDirectory;
    try {
      workingDirectory = resolveWorkspace(project);
    } catch (WorkspaceException ex) {
      onResolveFailure(project, build, buildLog);
      return new ValidationResult(false, null);
    }
    return validateWorkspace(workingDirectory, project, build, buildLog);
  }

  private File resolveWorkspace(final Project project) {
    final var projectWorkspace = workspaceService.ensureExists(project);
    return projectWorkspace.toFile();
  }

  private ValidationResult validateWorkspace(
      final File dir, final Project project, final Build build, final StringBuilder buildLog) {
    if (!dir.exists() || !dir.isDirectory()) {
      buildLog.append("\nBUILD FAILED:\nNo such file or directory");
      buildLifecycleService.complete(build, FAILED, buildLog);
      logWorkingDirError(project, dir.getAbsolutePath());
      return new ValidationResult(false, null);
    }
    return new ValidationResult(true, dir);
  }

  private void onResolveFailure(
      final Project project, final Build build, final StringBuilder buildLog) {
    buildLog.append("\nBUILD FAILED:\nNo such file or directory");
    buildLifecycleService.complete(build, FAILED, buildLog);
    try {
      final var workspacePath = workspaceService.resolve(project);
      logWorkingDirError(project, workspacePath.toAbsolutePath().toString());
    } catch (WorkspaceException inner) {
      logWorkingDirError(project, null);
    }
  }

  private void logWorkingDirError(final Project project, final String pathText) {
    if (pathText != null) {
      LOGGER.error(
          "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory: {}",
          project.getName(),
          pathText);
    } else {
      LOGGER.error(
          "[[ERROR]] Build process failed for project: {} - working directory does not exist or is not a directory",
          project.getName());
    }
  }
}
