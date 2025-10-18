package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.nio.file.Files.createDirectories;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceServiceTest {

  @TempDir File tempDir;
  private WorkspaceService service;
  private WorkspaceProperties props;

  @BeforeEach
  void setUp() {
    props = new WorkspaceProperties();
    props.setBaseDir(new File(tempDir, "workspaces").getAbsolutePath());
    service = new WorkspaceService(props);
  }

  private static Project projectWithRepoName(String repoName) {
    var project = new Project();
    project.setRepositoryName(repoName);
    return project;
  }

  @Test
  @DisplayName("Given missing baseDir, when resolving, then throw WorkspaceException")
  void missingBaseDirFails() {
    var project = projectWithRepoName("p1");
    props.setBaseDir(null);
    assertThatThrownBy(() -> service.resolveWorkspacePath(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("workspace.baseDir not configured");
  }

  @Test
  @DisplayName("Given blank baseDir, when resolving, then throw WorkspaceException")
  void blankBaseDirFails() {
    var project = projectWithRepoName("p1");
    props.setBaseDir("  ");
    assertThatThrownBy(() -> service.resolveWorkspacePath(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("workspace.baseDir not configured");
  }

  @Test
  @DisplayName("Given baseDir is a file, when resolving, then throw")
  void baseDirIsFileFails() throws IOException {
    File baseFile = new File(tempDir, "base-file.txt");
    Files.writeString(baseFile.toPath(), "x");
    props.setBaseDir(baseFile.getAbsolutePath());

    assertThatThrownBy(() -> service.resolveWorkspacePath(projectWithRepoName("p2")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("does not exist or is not a directory");
  }

  @Test
  @DisplayName("Given projectDir exists as a file, when ensuring, then throw")
  void projectDirIsFileFails() throws IOException {
    Path base = new File(tempDir, "base").toPath();
    createDirectories(base);
    props.setBaseDir(base.toString());

    Path filePath = base.resolve("p3");
    Files.writeString(filePath, "x");

    assertThatThrownBy(() -> service.ensureWorkspaceFor(projectWithRepoName("p3")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("not a directory");
  }

  @Test
  @DisplayName("Given symlink escape, when ensuring, then throw outside baseDir")
  void symlinkEscapeFails() throws IOException {
    Path base = new File(tempDir, "base2").toPath();
    Path outside = new File(tempDir, "outside").toPath();
    createDirectories(base);
    createDirectories(outside);
    props.setBaseDir(base.toString());

    Path evil = base.resolve("evil");
    Files.createSymbolicLink(evil, outside);

    assertThatThrownBy(() -> service.ensureWorkspaceFor(projectWithRepoName("evil")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("outside of base directory");
  }

  @Test
  @DisplayName("Given missing repository name, when resolving, then throw")
  void missingRepositoryNameFails() {
    Path base = new File(tempDir, "base3").toPath();
    assertThat(base.toFile().mkdirs()).isTrue();
    props.setBaseDir(base.toString());

    var project = new Project();
    project.setRepositoryName(null);

    assertThatThrownBy(() -> service.resolveWorkspacePath(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("Repository Name is missing");
  }

  @Test
  @DisplayName("Given isValid setup, ensureExists creates dir and returns canonical path")
  void ensureCreatesAndReturnsCanonical() throws IOException {
    Path base = new File(tempDir, "base4").toPath();
    createDirectories(base);
    props.setBaseDir(base.toString());

    var project = projectWithRepoName("proj-1");
    Path result = service.ensureWorkspaceFor(project);

    assertThat(Files.isDirectory(result)).isTrue();
    Path canonicalBase = base.toRealPath();
    assertThat(result.startsWith(canonicalBase)).isTrue();
    assertThat(result.getFileName().toString()).isEqualTo("proj-1");
  }
}
