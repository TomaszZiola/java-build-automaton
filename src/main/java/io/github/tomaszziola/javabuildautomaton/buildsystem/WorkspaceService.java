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

  private static final String ERR_BASEDIR_NOT_CONFIGURED = "workspace.baseDir not configured";
  private static final String ERR_BASEDIR_INVALID =
      "Workspace base directory does not exist or is not a directory: ";
  private static final String ERR_BASEDIR_RESOLVE_FAILED = "Failed to resolve baseDir: ";
  private static final String ERR_REPO_NAME_MISSING =
      "Repository Name is missing; cannot resolve workspace path";
  private static final String ERR_WORKSPACE_NOT_DIR =
      "Workspace path exists but is not a directory: ";
  private static final String ERR_CREATE_DIR_FAILED =
      "Failed to create project workspace directory: ";
  private static final String ERR_CANONICAL_RESOLVE_FAILED =
      "Failed to resolve canonical workspace path for target: ";
  private static final String ERR_TARGET_NOT_DIR = "Workspace path is not a directory: ";
  private static final String ERR_TARGET_OUTSIDE_BASE =
      "Workspace path resolved outside of base directory. baseDir=";

  private final WorkspaceProperties properties;

  public Path ensureWorkspaceFor(Project project) {
    var workspacePath = resolveWorkspacePath(project);
    createDirectoriesIfMissing(workspacePath);
    var canonical = resolveCanonicalPaths(workspacePath);
    validateProjectPathWithinBase(canonical.base(), canonical.project());
    return canonical.project();
  }

  public Path resolveWorkspacePath(Project project) {
    var basePath = resolveConfiguredBaseDir();
    var repoName = requireRepositoryName(project);
    var projectDir = basePath.resolve(repoName).normalize();
    if (exists(projectDir) && !isDirectory(projectDir)) {
      throw new WorkspaceException(ERR_WORKSPACE_NOT_DIR + projectDir);
    }
    return projectDir;
  }

  private Path resolveConfiguredBaseDir() {
    var base = properties.getBaseDir();
    if (!hasText(base)) {
      throw new WorkspaceException(ERR_BASEDIR_NOT_CONFIGURED);
    }
    var path = get(base).toAbsolutePath().normalize();
    if (!isDirectory(path)) {
      throw new WorkspaceException(ERR_BASEDIR_INVALID + path);
    }
    try {
      return path.toRealPath(NOFOLLOW_LINKS);
    } catch (IOException e) {
      throw new WorkspaceException(ERR_BASEDIR_RESOLVE_FAILED + path, e);
    }
  }

  private String requireRepositoryName(Project project) {
    var repoName = project.getRepositoryName();
    if (!hasText(repoName)) {
      throw new WorkspaceException(ERR_REPO_NAME_MISSING);
    }
    return repoName;
  }

  private CanonicalPaths resolveCanonicalPaths(Path workspacePath) {
    try {
      var canonicalBase = get(properties.getBaseDir()).toRealPath(); // follow links
      var canonicalProject = workspacePath.toRealPath(); // follow links
      return new CanonicalPaths(canonicalBase, canonicalProject);
    } catch (IOException e) {
      throw new WorkspaceException(ERR_CANONICAL_RESOLVE_FAILED + workspacePath, e);
    }
  }

  private void validateProjectPathWithinBase(Path canonicalBase, Path canonicalProject) {
    if (!isDirectory(canonicalProject)) {
      throw new WorkspaceException(ERR_TARGET_NOT_DIR + canonicalProject);
    }
    if (!canonicalProject.startsWith(canonicalBase)) {
      throw new WorkspaceException(
          ERR_TARGET_OUTSIDE_BASE + canonicalBase + " target=" + canonicalProject);
    }
  }

  private void createDirectoriesIfMissing(Path workspacePath) {
    try {
      if (!exists(workspacePath)) {
        createDirectories(workspacePath);
      }
    } catch (IOException e) {
      throw new WorkspaceException(ERR_CREATE_DIR_FAILED + workspacePath, e);
    }
  }

  private record CanonicalPaths(Path base, Path project) {}
}
