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

  public Path ensureExists(Project project) {
    var workspacePath = resolve(project);
    try {
      if (!exists(workspacePath)) {
        createDirectories(workspacePath);
      }
    } catch (IOException e) {
      throw new WorkspaceException(
          "Failed to create project workspace directory: " + workspacePath, e);
    }
    var canonical = resolveCanonical(workspacePath);
    validateWithinBase(canonical.base(), canonical.project());
    return canonical.project();
  }

  public Path resolve(Project project) {
    var basePath = resolveAndRequireBaseDir();
    var repoName = requiresRepositoryName(project);
    var projectDir = basePath.resolve(repoName).normalize();
    if (exists(projectDir) && !isDirectory(projectDir)) {
      throw new WorkspaceException("Workspace path exists but is not a directory: " + projectDir);
    }
    return projectDir;
  }

  private Path resolveAndRequireBaseDir() {
    var base = properties.getBaseDir();
    if (!hasText(base)) {
      throw new WorkspaceException("workspace.baseDir not configured");
    }
    var path = get(base).toAbsolutePath().normalize();
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

  private String requiresRepositoryName(Project project) {
    var repoName = project.getRepositoryName();
    if (!hasText(repoName)) {
      throw new WorkspaceException("Repository Name is missing; cannot resolve workspace path");
    }
    return repoName;
  }

  private CanonicalPaths resolveCanonical(Path workspacePath) {
    try {
      var canonicalBase = get(properties.getBaseDir()).toRealPath();
      var canonicalProject = workspacePath.toRealPath();
      return new CanonicalPaths(canonicalBase, canonicalProject);
    } catch (IOException e) {
      throw new WorkspaceException(
          "Failed to resolve canonical workspace path for target: " + workspacePath, e);
    }
  }

  private void validateWithinBase(Path canonicalBase, Path canonicalProject) {
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
