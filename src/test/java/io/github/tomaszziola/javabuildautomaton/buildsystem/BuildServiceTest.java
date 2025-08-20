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
import org.mockito.ArgumentCaptor;

class BuildServiceTest extends BaseUnit {
  @Test
  void givenExistingWorkingDirectory_whenStartBuildProcess_thenSavesSuccessfulBuildAndLogs()
      throws IOException, InterruptedException {
    // given
    when(processExecutor.execute(any(File.class), any(String[].class)))
        .thenReturn(new ExecutionResult(true, "git pulled\nbuild ok\n"));

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    final ArgumentCaptor<Build> captor = ArgumentCaptor.forClass(Build.class);
    verify(buildRepository, times(2)).save(captor.capture());
    final Build finalBuild = captor.getAllValues().getLast();
    assertThat(finalBuild.getStatus()).isEqualTo(SUCCESS);
    assertThat(finalBuild.getLogs()).isEqualTo("git pulled\nbuild ok\n");
  }

  @Test
  void givenNonExistingWorkingDirectory_whenStartBuildProcess_thenSavesFailedBuildAndLogs()
      throws IOException, InterruptedException {
    // given
    project = ProjectModel.basic(nonExistentPath);
    when(processExecutor.execute(any(File.class), any(String[].class)))
        .thenThrow(new IOException("No such file or directory"));

    // when
    assertDoesNotThrow(() -> buildServiceImpl.startBuildProcess(project));

    // then
    final ArgumentCaptor<Build> captor = ArgumentCaptor.forClass(Build.class);
    verify(buildRepository, times(2)).save(captor.capture());
    final Build finalBuild = captor.getAllValues().get(captor.getAllValues().size() - 1);
    assertThat(finalBuild.getStatus()).isEqualTo(FAILED);
    assertThat(finalBuild.getLogs()).isEqualTo("\nBUILD FAILED:\nNo such file or directory");
  }
}
