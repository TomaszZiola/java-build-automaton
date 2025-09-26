package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.Paths.get;
import static org.springframework.util.StringUtils.hasText;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceService {

  private final WorkspaceProperties properties;

  public WorkspaceService(final WorkspaceProperties properties) {
    this.properties = properties;
  }

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

  private String requireSlug(final Project project) {
    final var slug = project.getSlug();
    if (!hasText(slug)) {
      throw new WorkspaceException("Project slug is missing; cannot resolve workspace path");
    }
    return slug;
  }

  public Path resolve(final Project project) {
    final var base = requireExistingBase();
    final var slug = requireSlug(project);
    final var projectDir = base.resolve(slug).normalize();
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
      // Resolve base canonical path following symlinks to avoid false negatives on systems where /var is a symlink
      final var canonicalBase = get(properties.getBaseDir()).toRealPath();
      // Follow symlinks for project path to detect symlink escapes outside base directory
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
