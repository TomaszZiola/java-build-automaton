package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BuildServiceTest extends BaseUnit {
  private static final String TEMP_PREFIX = "ws-";

  @Test
  @DisplayName(
      "Given invalid workspace, when starting build process, then stop early and no git/build calls")
  void stopsEarlyWhenWorkspaceInvalid() {
    // given
    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(false, null));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner, never()).pull(any());
    verify(gitCommandRunner, never()).cloneRepo(any(), any());
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo not initialized and clone succeeds, when building, then complete SUCCESS with aggregated logs")
  void cloneSuccessThenBuildSuccess() throws Exception {
    // given
    final File workingDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(true, workingDir));

    when(gitCommandRunner.cloneRepo(project.getRepositoryUrl(), workingDir))
        .thenReturn(pullExecutionResult);
    when(buildExecutor.build(project.getBuildTool(), workingDir)).thenReturn(buildExecutionResult);

    final ArgumentCaptor<CharSequence> logsCaptor = ArgumentCaptor.forClass(CharSequence.class);

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner).cloneRepo(project.getRepositoryUrl(), workingDir);
    verify(buildExecutor).build(project.getBuildTool(), workingDir);
    verify(buildLifecycleService)
        .complete(any(Build.class), org.mockito.ArgumentMatchers.eq(SUCCESS), logsCaptor.capture());

    final String logs = logsCaptor.getValue().toString();
    assertThat(logs).contains("pull's ok");
    assertThat(logs).contains("build's ok");
  }

  @Test
  @DisplayName(
      "Given repo not initialized and clone fails, when building, then complete FAILED and do not build")
  void cloneFailureStopsProcess() throws Exception {
    // given
    final File workingDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(true, workingDir));

    when(gitCommandRunner.cloneRepo(project.getRepositoryUrl(), workingDir))
        .thenReturn(new ExecutionResult(false, "clone failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService)
        .complete(
            any(Build.class), org.mockito.ArgumentMatchers.eq(FAILED), any(CharSequence.class));
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull fails, when building, then complete FAILED and do not build")
  void pullFailureStopsProcess() throws Exception {
    // given - create .git directory
    final File workingDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    final File gitDir = new File(workingDir, ".git");
    assertThat(gitDir.mkdir()).isTrue();

    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(true, workingDir));

    when(gitCommandRunner.pull(workingDir)).thenReturn(new ExecutionResult(false, "pull failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService)
        .complete(
            any(Build.class), org.mockito.ArgumentMatchers.eq(FAILED), any(CharSequence.class));
    verify(buildExecutor, never()).build(any(), any());
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull succeeds but build fails, when building, then complete FAILED")
  void buildFailureAfterSuccessfulPull() throws Exception {
    // given - create .git directory
    final File workingDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    final File gitDir = new File(workingDir, ".git");
    assertThat(gitDir.mkdir()).isTrue();

    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(true, workingDir));

    when(gitCommandRunner.pull(workingDir)).thenReturn(pullExecutionResult);
    when(buildExecutor.build(project.getBuildTool(), workingDir))
        .thenReturn(new ExecutionResult(false, "build failed\n"));

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(buildLifecycleService)
        .complete(
            any(Build.class), org.mockito.ArgumentMatchers.eq(FAILED), any(CharSequence.class));
  }

  @Test
  @DisplayName(
      "Given repo initialized and pull/build succeed, when building, then complete SUCCESS with aggregated logs")
  void pullSuccessThenBuildSuccess() throws Exception {
    // given - create .git directory
    final File workingDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    final File gitDir = new File(workingDir, ".git");
    assertThat(gitDir.mkdir()).isTrue();

    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(true, workingDir));

    when(gitCommandRunner.pull(workingDir)).thenReturn(pullExecutionResult);
    when(buildExecutor.build(project.getBuildTool(), workingDir)).thenReturn(buildExecutionResult);

    final ArgumentCaptor<CharSequence> logsCaptor = ArgumentCaptor.forClass(CharSequence.class);

    // when
    buildServiceImpl.startBuildProcess(project);

    // then
    verify(gitCommandRunner).pull(workingDir);
    verify(buildExecutor).build(project.getBuildTool(), workingDir);
    verify(buildLifecycleService)
        .complete(any(Build.class), org.mockito.ArgumentMatchers.eq(SUCCESS), logsCaptor.capture());

    final String logs = logsCaptor.getValue().toString();
    assertThat(logs).contains("pull's ok");
    assertThat(logs).contains("build's ok");
  }

  @Test
  @DisplayName(
      "Given build id exists, when executing build by id, then mark in progress and run flow")
  void executeBuildByIdMarksInProgress() {
    // given
    build.setProject(project);
    when(workingDirectoryValidator.prepareWorkspace(any(), any(), any()))
        .thenReturn(new ValidationResult(false, null));

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
        .isInstanceOf(BuildNotFoundException.class);
  }
}
