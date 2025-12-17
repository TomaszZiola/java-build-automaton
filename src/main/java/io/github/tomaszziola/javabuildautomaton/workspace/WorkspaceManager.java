package io.github.tomaszziola.javabuildautomaton.workspace;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.springframework.util.StringUtils.hasText;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceManager {
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
  private static final String TARGET_EQ = " target=";

  private final Path baseDir;

  public WorkspaceManager(WorkspaceProperties properties) {
    this.baseDir = validateBaseDir(properties.getBaseDir());
  }

  private Path validateBaseDir(Path base) {
    if (base == null) {
      throw new WorkspaceException(ERR_BASEDIR_NOT_CONFIGURED);
    }
    var path = base.toAbsolutePath().normalize();
    if (!isDirectory(path)) {
      throw new WorkspaceException(ERR_BASEDIR_INVALID + path);
    }
    try {
      return path.toRealPath(NOFOLLOW_LINKS);
    } catch (IOException e) {
      throw new WorkspaceException(ERR_BASEDIR_RESOLVE_FAILED + path, e);
    }
  }

  public Path ensureWorkspaceFor(Project project) {
    var projectDir = resolveProjectWorkspacePath(project);
    ensureDirectoryExists(projectDir);
    var canonical = resolveCanonicalBaseAndProject(projectDir);
    assertProjectWithinBaseDir(canonical.base(), canonical.project());
    return canonical.project();
  }

  public Path resolveProjectWorkspacePath(Project project) {
    var repositoryName = requireRepositoryName(project);
    var projectDir = baseDir.resolve(repositoryName).normalize();
    if (exists(projectDir) && !isDirectory(projectDir)) {
      throw new WorkspaceException(ERR_WORKSPACE_NOT_DIR + projectDir);
    }
    return projectDir;
  }

  private String requireRepositoryName(Project project) {
    var repo = project.getRepositoryName();
    if (!hasText(repo)) {
      throw new WorkspaceException(ERR_REPO_NAME_MISSING);
    }
    return repo;
  }

  private CanonicalPaths resolveCanonicalBaseAndProject(Path workspacePath) {
    try {
      var canonicalProject = workspacePath.toRealPath(NOFOLLOW_LINKS);
      return new CanonicalPaths(baseDir, canonicalProject);
    } catch (IOException e) {
      throw new WorkspaceException(ERR_CANONICAL_RESOLVE_FAILED + workspacePath, e);
    }
  }

  private void assertProjectWithinBaseDir(Path canonicalBase, Path canonicalProject) {
    if (!isDirectory(canonicalProject)) {
      throw new WorkspaceException(ERR_TARGET_NOT_DIR + canonicalProject);
    }
    if (!canonicalProject.startsWith(canonicalBase)) {
      throw new WorkspaceException(
          ERR_TARGET_OUTSIDE_BASE + canonicalBase + TARGET_EQ + canonicalProject);
    }
  }

  private void ensureDirectoryExists(Path workspacePath) {
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
