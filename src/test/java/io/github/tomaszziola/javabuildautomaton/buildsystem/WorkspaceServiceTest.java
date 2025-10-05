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

  private static Project projectWithRepoName(final String repoName) {
    final var project = new Project();
    project.setRepositoryName(repoName);
    return project;
  }

  @Test
  @DisplayName("Given missing baseDir, when resolving, then throw WorkspaceException")
  void missingBaseDirFails() {
    final var project = projectWithRepoName("p1");
    props.setBaseDir(null);
    assertThatThrownBy(() -> service.resolve(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("workspace.baseDir not configured");
  }

  @Test
  @DisplayName("Given blank baseDir, when resolving, then throw WorkspaceException")
  void blankBaseDirFails() {
    final var project = projectWithRepoName("p1");
    props.setBaseDir("  ");
    assertThatThrownBy(() -> service.resolve(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("workspace.baseDir not configured");
  }

  @Test
  @DisplayName("Given baseDir is a file, when resolving, then throw")
  void baseDirIsFileFails() throws IOException {
    final File baseFile = new File(tempDir, "base-file.txt");
    Files.writeString(baseFile.toPath(), "x");
    props.setBaseDir(baseFile.getAbsolutePath());

    assertThatThrownBy(() -> service.resolve(projectWithRepoName("p2")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("does not exist or is not a directory");
  }

  @Test
  @DisplayName("Given projectDir exists as a file, when ensuring, then throw")
  void projectDirIsFileFails() throws IOException {
    final Path base = new File(tempDir, "base").toPath();
    createDirectories(base);
    props.setBaseDir(base.toString());

    final Path filePath = base.resolve("p3");
    Files.writeString(filePath, "x");

    assertThatThrownBy(() -> service.ensureExists(projectWithRepoName("p3")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("not a directory");
  }

  @Test
  @DisplayName("Given symlink escape, when ensuring, then throw outside baseDir")
  void symlinkEscapeFails() throws IOException {
    final Path base = new File(tempDir, "base2").toPath();
    final Path outside = new File(tempDir, "outside").toPath();
    createDirectories(base);
    createDirectories(outside);
    props.setBaseDir(base.toString());

    // Create symlink inside base pointing to outside
    final Path evil = base.resolve("evil");
    Files.createSymbolicLink(evil, outside);

    assertThatThrownBy(() -> service.ensureExists(projectWithRepoName("evil")))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("outside of base directory");
  }

  @Test
  @DisplayName("Given missing slug, when resolving, then throw")
  void missingSlugFails() {
    final Path base = new File(tempDir, "base3").toPath();
    assertThat(base.toFile().mkdirs()).isTrue();
    props.setBaseDir(base.toString());

    final var project = new Project();
    project.setRepositoryName(null);

    assertThatThrownBy(() -> service.resolve(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("slug is missing");
  }

  @Test
  @DisplayName("Given isValid setup, ensureExists creates dir and returns canonical path")
  void ensureCreatesAndReturnsCanonical() throws IOException {
    final Path base = new File(tempDir, "base4").toPath();
    createDirectories(base);
    props.setBaseDir(base.toString());

    final var project = projectWithRepoName("proj-1");
    final Path result = service.ensureExists(project);

    assertThat(Files.isDirectory(result)).isTrue();
    final Path canonicalBase = base.toRealPath();
    assertThat(result.startsWith(canonicalBase)).isTrue();
    assertThat(result.getFileName().toString()).isEqualTo("proj-1");
  }
}
