package io.github.tomaszziola.javabuildautomaton.workspace;

import static io.github.tomaszziola.javabuildautomaton.utils.SetterUtils.setField;
import static java.nio.file.FileSystems.getDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkspaceManagerTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given missing repository name, when resolving workspace path, then throw WorkspaceException")
  void throwsWhenRepositoryNameMissing() {
    setField(project, Project::setRepositoryName, " ");

    assertThatThrownBy(() -> workspaceManagerImpl.resolveProjectWorkspacePath(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessage("Repository Name is missing; cannot resolve workspace path");
  }

  @Test
  @DisplayName("Given blank baseDir, when resolving workspace, then throw WorkspaceException")
  void throwsWhenBaseDirBlank() {
    setField(workspacePropertiesImpl, WorkspaceProperties::setBaseDir, null);

    assertThatThrownBy(() -> new WorkspaceManager(workspacePropertiesImpl))
        .isInstanceOf(WorkspaceException.class)
        .hasMessage("workspace.baseDir not configured");
  }

  @Test
  @DisplayName("Given base path is a file, when resolving workspace, then throw WorkspaceException")
  void throwsWhenBaseDirNotDirectory() {
    setField(workspacePropertiesImpl, WorkspaceProperties::setBaseDir, Path.of("base.txt"));

    assertThatThrownBy(() -> new WorkspaceManager(workspacePropertiesImpl))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageStartingWith("Workspace base directory does not exist or is not a directory: ");
  }

  @Test
  @DisplayName(
      "Given child path exists as file, when resolving project workspace, then throw WorkspaceException")
  void throwsWhenResolvedProjectPathIsExistingFile() throws IOException {
    var file = tempDir.resolve("java-build-automaton");
    Files.writeString(file, "x");

    assertThatThrownBy(() -> workspaceManagerImpl.resolveProjectWorkspacePath(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessage("Workspace path exists but is not a directory: " + file.toAbsolutePath());
  }

  @Test
  @DisplayName(
      "Given valid base and repo, when ensuring workspace, then create directory and keep within base")
  void ensureWorkspaceCreatesDirectoryWithinBase() {
    var result = workspaceManagerImpl.ensureWorkspaceFor(project);

    assertThat(result).isDirectory().startsWith(tempDir.toAbsolutePath());
  }

  @Test
  @DisplayName(
      "Given repo resolves outside base, when ensuring workspace, then throw WorkspaceException")
  void ensureWorkspaceThrowsWhenOutsideBase() {
    setField(project, Project::setRepositoryName, ".." + getDefault().getSeparator() + "outside");

    assertThatThrownBy(() -> workspaceManagerImpl.ensureWorkspaceFor(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageContaining("Workspace path resolved outside of base directory.")
        .hasMessageContaining("baseDir=")
        .hasMessageContaining("target=");
  }

  @Test
  @DisplayName(
      "Given parent is a file, when creating workspace, then wrap IOException into WorkspaceException")
  void ensureWorkspaceWrapsCreateDirectoriesFailure() throws IOException {
    var parentFile = tempDir.resolve("fileParent");
    Files.writeString(parentFile, "x");

    setField(project, Project::setRepositoryName, "fileParent/child");

    assertThatThrownBy(() -> workspaceManagerImpl.ensureWorkspaceFor(project))
        .isInstanceOf(WorkspaceException.class)
        .hasMessageStartingWith("Failed to create project workspace directory: ");
  }
}
