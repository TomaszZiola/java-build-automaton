package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.Paths.get;
import static org.springframework.util.StringUtils.hasText;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

  private final WorkspaceProperties properties;

  private Path requireExistingBase() {
    final var base = properties.getBaseDir();
    if (!hasText(base)) {
      throw new WorkspaceException("workspace.baseDir not configured");
    }
    final var path = get(base).toAbsolutePath().normalize();
    if (!isDirectory(path)) {
      throw new WorkspaceException(
          "Workspace base directory does not exist or is not a directory: " + path);
    }
    try {
      return path.toRealPath(NOFOLLOW_LINKS);
    } catch (IOException e) {
      throw new WorkspaceException("Failed to resolve baseDir: " + path, e);
    }
  }

  private String requiresRepositoryName(final Project project) {
    final var repoName = project.getRepositoryName();
    if (!hasText(repoName)) {
      throw new WorkspaceException("Project slug is missing; cannot resolve workspace path");
    }
    return repoName;
  }

  public Path resolve(final Project project) {
    final var base = requireExistingBase();
    final var repoName = requiresRepositoryName(project);
    final var projectDir = base.resolve(repoName).normalize();
    if (exists(projectDir) && !isDirectory(projectDir)) {
      throw new WorkspaceException("Workspace path exists but is not a directory: " + projectDir);
    }
    return projectDir;
  }

  public Path ensureExists(final Project project) {
    final var workspacePath = resolve(project);
    try {
      if (!exists(workspacePath)) {
        createDirectories(workspacePath);
      }
    } catch (IOException e) {
      throw new WorkspaceException(
          "Failed to create project workspace directory: " + workspacePath, e);
    }
    final var canonical = resolveCanonical(workspacePath);
    validateWithinBase(canonical.base(), canonical.project());
    return canonical.project();
  }

  private CanonicalPaths resolveCanonical(final Path workspacePath) {
    try {
      final var canonicalBase = get(properties.getBaseDir()).toRealPath();
      final var canonicalProject = workspacePath.toRealPath();
      return new CanonicalPaths(canonicalBase, canonicalProject);
    } catch (IOException e) {
      throw new WorkspaceException(
          "Failed to resolve canonical workspace path for target: " + workspacePath, e);
    }
  }

  private void validateWithinBase(final Path canonicalBase, final Path canonicalProject) {
    if (!isDirectory(canonicalProject)) {
      throw new WorkspaceException("Workspace path is not a directory: " + canonicalProject);
    }
    if (!canonicalProject.startsWith(canonicalBase)) {
      throw new WorkspaceException(
          "Workspace path resolved outside of base directory. baseDir="
              + canonicalBase
              + " target="
              + canonicalProject);
    }
  }
}
