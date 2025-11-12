package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.workspace.BuildWorkspaceGuard;
import io.github.tomaszziola.javabuildautomaton.workspace.WorkspaceManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class BuildWorkspaceGuardTest {

  @TempDir File tempDir;

  @Test
  @DisplayName(
      "Given workspace resolves to existing directory, when preparing, then isValid and no lifecycle completion")
  void returnsValidWhenWorkspaceExists() {
    // given
    final WorkspaceManager workspaceManager = Mockito.mock(WorkspaceManager.class);
    final BuildLifecycleService lifecycle = Mockito.mock(BuildLifecycleService.class);

    final BuildWorkspaceGuard validator = new BuildWorkspaceGuard(lifecycle, workspaceManager);

    final Project project = new Project();
    final Build build = new Build();

    final File workspaceDir = new File(tempDir, "proj-ok");
    assertThat(workspaceDir.mkdirs()).isTrue();

    when(workspaceManager.ensureWorkspaceFor(project)).thenReturn(workspaceDir.toPath());

    final StringBuilder logs = new StringBuilder();

    // when
    final ValidationResult result = validator.prepareWorkspaceOrFail(project, build, logs);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.workingDirectory()).isEqualTo(workspaceDir);
    verify(lifecycle, never()).complete(any(Build.class), any(), any(CharSequence.class));
  }

  @Test
  @DisplayName(
      "Given ensureExists throws, when preparing, then complete FAILED and include failure logs")
  void completesFailedWhenEnsureThrows() {
    // given
    final WorkspaceManager workspaceManager = Mockito.mock(WorkspaceManager.class);
    final BuildLifecycleService lifecycle = Mockito.mock(BuildLifecycleService.class);
    final BuildWorkspaceGuard validator = new BuildWorkspaceGuard(lifecycle, workspaceManager);

    final Project project = new Project();
    final Build build = new Build();

    when(workspaceManager.ensureWorkspaceFor(project)).thenThrow(new WorkspaceException("boom"));
    when(workspaceManager.resolveProjectWorkspacePath(project))
        .thenThrow(new WorkspaceException("boom2"));

    final StringBuilder logs = new StringBuilder();

    // when
    final ValidationResult result = validator.prepareWorkspaceOrFail(project, build, logs);

    // then
    assertThat(result.isValid()).isFalse();

    final ArgumentCaptor<CharSequence> logsCaptor = ArgumentCaptor.forClass(CharSequence.class);
    verify(lifecycle).complete(any(Build.class), Mockito.eq(FAILED), logsCaptor.capture());

    final String aggregated = logsCaptor.getValue().toString();
    assertThat(aggregated).contains("BUILD FAILED");
  }

  @Test
  @DisplayName("Given ensureExists returns non-directory, when preparing, then complete FAILED")
  void completesFailedWhenPathIsNotDirectory() throws Exception {
    // given
    final WorkspaceManager workspaceManager = Mockito.mock(WorkspaceManager.class);
    final BuildLifecycleService lifecycle = Mockito.mock(BuildLifecycleService.class);
    final BuildWorkspaceGuard validator = new BuildWorkspaceGuard(lifecycle, workspaceManager);

    final Project project = new Project();
    final Build build = new Build();

    final Path filePath = new File(tempDir, "not-a-dir.txt").toPath();
    Files.writeString(filePath, "x");

    when(workspaceManager.ensureWorkspaceFor(project)).thenReturn(filePath);
    when(workspaceManager.resolveProjectWorkspacePath(project)).thenReturn(filePath);

    final StringBuilder logs = new StringBuilder();

    // when
    final ValidationResult result = validator.prepareWorkspaceOrFail(project, build, logs);

    // then
    assertThat(result.isValid()).isFalse();
    verify(lifecycle).complete(any(Build.class), Mockito.eq(FAILED), any(CharSequence.class));
  }
}
