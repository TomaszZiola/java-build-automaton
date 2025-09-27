package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.QUEUED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BuildLifecycleServiceTest {

  @Test
  @DisplayName("createInProgress sets status and start time, then saves")
  void createInProgressSetsFieldsAndSaves() {
    // given
    final BuildRepository repo = Mockito.mock(BuildRepository.class);
    final BuildLifecycleService service = new BuildLifecycleService(repo);
    when(repo.save(any(Build.class))).thenAnswer(inv -> inv.getArgument(0));

    final Project project = new Project();

    // when
    final Build build = service.createInProgress(project);

    // then
    assertThat(build.getProject()).isSameAs(project);
    assertThat(build.getStatus()).isEqualTo(IN_PROGRESS);
    assertThat(build.getStartTime()).isNotNull();
    verify(repo, times(1)).save(any(Build.class));
  }

  @Test
  @DisplayName("createQueued sets status QUEUED and start time, then saves")
  void createQueuedSetsFieldsAndSaves() {
    // given
    final BuildRepository repo = Mockito.mock(BuildRepository.class);
    final BuildLifecycleService service = new BuildLifecycleService(repo);
    when(repo.save(any(Build.class))).thenAnswer(inv -> inv.getArgument(0));

    final Project project = new Project();

    // when
    final Build build = service.createQueued(project);

    // then
    assertThat(build.getProject()).isSameAs(project);
    assertThat(build.getStatus()).isEqualTo(QUEUED);
    assertThat(build.getStartTime()).isNotNull();
    verify(repo, times(1)).save(any(Build.class));
  }

  @Test
  @DisplayName("complete sets status, end time and logs, then saves")
  void completeSetsFieldsAndSaves() {
    // given
    final BuildRepository repo = Mockito.mock(BuildRepository.class);
    final BuildLifecycleService service = new BuildLifecycleService(repo);

    final Build build = new Build();
    build.setStartTime(Instant.now());

    // when
    service.complete(build, SUCCESS, new StringBuilder("all good\n"));

    // then
    assertThat(build.getStatus()).isEqualTo(SUCCESS);
    assertThat(build.getEndTime()).isNotNull();
    assertThat(build.getLogs()).isEqualTo("all good\n");
    verify(repo).save(build);
  }

  @Test
  @DisplayName("markInProgress sets status and saves")
  void markInProgressSetsStatusAndSaves() {
    // given
    final BuildRepository repo = Mockito.mock(BuildRepository.class);
    final BuildLifecycleService service = new BuildLifecycleService(repo);

    final Build build = new Build();

    // when
    service.markInProgress(build);

    // then
    assertThat(build.getStatus()).isEqualTo(IN_PROGRESS);
    verify(repo).save(build);
  }
}
