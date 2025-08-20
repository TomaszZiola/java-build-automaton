package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class BuildServiceTest extends BaseUnit {
  @Test
  void givenExistingWorkingDirectory_whenStartBuildProcess_thenSavesSuccessfulBuildAndLogs() {
    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final Build finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(SUCCESS);
    assertThat(finalBuild.getLogs()).isEqualTo(successExecutionResult.logs());
  }

  @Test
  void givenNonExistingWorkingDirectory_whenStartBuildProcess_thenSavesFailedBuildAndLogs()
      throws IOException, InterruptedException {
    // given
    project = ProjectModel.basic(nonExistentPath);

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final Build finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(finalBuild.getLogs()).isEqualTo("\nBUILD FAILED:\nNo such file or directory");
  }

  @Test
  void givenGitPullFails_whenStartBuildProcess_thenSavesFailedBuildAndDoesNotRunBuildTool()
      throws IOException, InterruptedException {
    // given
    when(gitCommandRunner.pull(any(File.class)))
        .thenReturn(new ExecutionResult(false, "pull failed"));

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    verify(gitCommandRunner, times(1)).pull(any(File.class));
    verify(buildExecutor, times(0)).build(any(BuildTool.class), any(File.class));

    verify(buildRepository, times(2)).save(buildCaptor.capture());
    final Build finalBuild = buildCaptor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(finalBuild.getLogs()).isEqualTo("pull failed");
  }
}
