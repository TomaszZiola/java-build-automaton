package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuildServiceTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given invalid workspace, when starting build process, then stop early and no git/build calls")
  void stopsEarlyWhenWorkspaceInvalid() {
    // given\
    when(workingDirectoryValidator.prepareWorkspace(
            eq(project), eq(build), isA(StringBuilder.class)))
        .thenReturn(new ValidationResult(false, null));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner, never()).pull(any());
    verify(gitCommandRunner, never()).clone(any(), any());
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo not initialized and clone succeeds, when building, then complete SUCCESS with aggregated logs")
  void cloneSuccessThenBuildSuccess() {
    // given
    final var logsCaptor = forClass(CharSequence.class);

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner).clone(project.getRepositoryUrl(), workingDir);
    verify(buildExecutor).build(project.getBuildTool(), workingDir);
    verify(buildLifecycleService).complete(any(Build.class), eq(SUCCESS), logsCaptor.capture());

    final String logs = logsCaptor.getValue().toString();
    assertThat(logs).contains("clone's ok");
    assertThat(logs).contains("build's ok");
  }

  @Test
  @DisplayName(
      "Given repo not initialized and clone fails, when building, then complete FAILED and do not build")
  void cloneFailureStopsProcess() {
    // given
    when(gitCommandRunner.clone(project.getRepositoryUrl(), workingDir))
        .thenReturn(new ExecutionResult(false, "clone failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService).complete(any(Build.class), eq(FAILED), any(CharSequence.class));
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull fails, when building, then complete FAILED and do not build")
  void pullFailureStopsProcess() {
    // given
    final var gitDir = new File(workingDir, ".git");
    assertThat(gitDir.mkdir()).isTrue();

    when(gitCommandRunner.pull(workingDir)).thenReturn(new ExecutionResult(false, "pull failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService).complete(any(Build.class), eq(FAILED), any(CharSequence.class));
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull succeeds but build fails, when building, then complete FAILED")
  void buildFailureAfterSuccessfulPull() {
    // given
    when(buildExecutor.build(project.getBuildTool(), workingDir))
        .thenReturn(new ExecutionResult(false, "build failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService).complete(any(Build.class), eq(FAILED), any(CharSequence.class));
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull/build succeed, when building, then complete SUCCESS with aggregated logs")
  void pullSuccessThenBuildSuccess() {
    // given
    final File gitDir = new File(workingDir, ".git");
    assertThat(gitDir.mkdir()).isTrue();

    final var logsCaptor = forClass(CharSequence.class);

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner).pull(workingDir);
    verify(buildExecutor).build(project.getBuildTool(), workingDir);
    verify(buildLifecycleService).complete(any(Build.class), eq(SUCCESS), logsCaptor.capture());

    final String logs = logsCaptor.getValue().toString();
    assertThat(logs).contains("pull's ok");
    assertThat(logs).contains("build's ok");
  }

  @Test
  @DisplayName(
      "Given build id exists, when executing build by id, then mark in progress and run flow")
  void executeBuildByIdMarksInProgress() {
    // when
    buildServiceImpl.executeBuild(buildId);

    // then
    verify(buildLifecycleService).markInProgress(build);
  }

  @Test
  @DisplayName(
      "Given missing build id, when executing build by id, then throw BuildNotFoundException")
  void executeBuildByIdNotFound() {
    assertThatThrownBy(() -> buildServiceImpl.executeBuild(nonExistentBuildId))
        .isInstanceOf(BuildNotFoundException.class)
        .hasMessageContaining(nonExistentBuildId.toString());
  }
}
