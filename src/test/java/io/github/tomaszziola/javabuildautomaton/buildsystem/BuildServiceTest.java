package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuildServiceTest extends BaseUnit {

  private static String normalizeEol(final String input) {
    return input == null ? null : input.replaceAll("\\R", "\n");
  }

  @Test
  @DisplayName(
      "Given existing working directory, when starting build process, then save successful build and log")
  void savesSuccessfulBuildAndLogsWhenWorkingDirectoryExists() {
    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final var finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(SUCCESS);
    assertThat(normalizeEol(finalBuild.getLogs())).isEqualTo("pull's ok\nbuild's ok\n");
  }

  @Test
  @DisplayName(
      "Given non-existing working directory, when starting build process, then save failed build and log")
  void savesFailedBuildAndLogsWhenWorkingDirectoryMissing() {
    // given
    project = ProjectModel.basic(nonExistentPath);

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final var finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(normalizeEol(finalBuild.getLogs()))
        .isEqualTo("\nBUILD FAILED:\nNo such file or directory");
  }

  @Test
  @DisplayName(
      "Given git pull fails, when starting build process, then save failed build and do not run build tool")
  void savesFailedBuildAndSkipsBuildToolWhenGitPullFails() {
    // given
    when(gitCommandRunner.pull(any(File.class)))
        .thenReturn(new ExecutionResult(false, "pull failed"));

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(gitCommandRunner, times(1)).pull(any(File.class));
    verify(buildExecutor, times(0)).build(any(BuildTool.class), any(File.class));

    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final var finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(normalizeEol(finalBuild.getLogs())).isEqualTo("pull failed");
  }

  @Test
  @DisplayName("Given build fails, when starting build process, then save failed build")
  void savesFailedBuildWhenBuildFails() {
    // given
    when(buildExecutor.build(eq(project.getBuildTool()), any(File.class)))
        .thenReturn(new ExecutionResult(false, "build failed"));

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(gitCommandRunner, times(1)).pull(any(File.class));
    verify(buildExecutor, times(1)).build(eq(GRADLE), any(File.class));

    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final var finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(normalizeEol(finalBuild.getLogs())).isEqualTo("pull's ok\nbuild failed");
  }

  @Test
  void givenExistingPathButNotDirectory_whenStartBuildProcess_thenValidationFailsAndExitsEarly()
      throws Exception {
    final var notADir = Files.createFile(tempDir.toPath().resolve("not-a-directory.txt")).toFile();
    project = ProjectModel.basic(notADir.getAbsolutePath());

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(gitCommandRunner, times(0)).pull(any(File.class));
    verify(buildExecutor, times(0)).build(eq(GRADLE), any(File.class));

    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final var finalBuild2 = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild2.getStatus()).isEqualTo(FAILED);
    assertThat(normalizeEol(finalBuild2.getLogs()))
        .isEqualTo("\nBUILD FAILED:\nNo such file or directory");
  }
}
