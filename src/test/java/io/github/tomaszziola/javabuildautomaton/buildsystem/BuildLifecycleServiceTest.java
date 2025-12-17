package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.QUEUED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuildLifecycleServiceTest extends BaseUnit {

  @Test
  @DisplayName("Given project, when makeInProgress, then set IN_PROGRESS and start time and save")
  void makeInProgressSetsStatusAndStartTimeThenSaves() {
    // when
    final var build = buildLifecycleServiceImpl.makeInProgress(project);

    // then
    assertThat(build.getProject()).isSameAs(project);
    assertThat(build.getStatus()).isEqualTo(IN_PROGRESS);
    assertThat(build.getStartTime()).isNotNull();
    verify(buildRepository, times(1)).save(any(Build.class));
  }

  @Test
  @DisplayName("Given project, when createQueued, then set QUEUED and start time and save")
  void createQueuedSetsStatusQueuedAndStartTimeThenSaves() {
    // when
    final var build = buildLifecycleServiceImpl.createQueued(project);

    // then
    assertThat(build.getProject()).isSameAs(project);
    assertThat(build.getStatus()).isEqualTo(QUEUED);
    assertThat(build.getStartTime()).isNotNull();
    verify(buildRepository, times(1)).save(any(Build.class));
  }

  @Test
  @DisplayName("Given build and logs, when complete, then set status, end time and logs and save")
  void completeSetsStatusEndTimeAndLogsThenSaves() {
    // when
    buildLifecycleServiceImpl.complete(build, SUCCESS, new StringBuilder("all good\n"));

    // then
    assertThat(build.getStatus()).isEqualTo(SUCCESS);
    assertThat(build.getEndTime()).isNotNull();
    assertThat(build.getLogs()).isEqualTo("all good\n");
    verify(buildRepository).save(build);
  }

  @Test
  @DisplayName("Given build, when markInProgress, then set status and save")
  void markInProgressSetsStatusThenSaves() {
    // when
    buildLifecycleServiceImpl.markInProgress(build);

    // then
    assertThat(build.getStatus()).isEqualTo(IN_PROGRESS);
    verify(buildRepository).save(build);
  }

  @Test
  @DisplayName("Given null logs, when complete, then set status and end time and keep logs null")
  void completeWithNullLogsSetsStatusAndEndTimeKeepsLogsNullThenSaves() {
    // when
    buildLifecycleServiceImpl.complete(build, SUCCESS, null);

    // then
    assertThat(build.getStatus()).isEqualTo(SUCCESS);
    assertThat(build.getEndTime()).isNotNull();
    assertThat(build.getLogs()).isNull();
    verify(buildRepository).save(build);
  }
}
