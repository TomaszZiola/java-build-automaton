package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.QUEUED;
import static java.time.Instant.now;

import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildLifecycleService {

  private final BuildRepository buildRepository;

  public Build createInProgress(final Project project) {
    final var build = new Build();
    build.setProject(project);
    build.setStartTime(now());
    build.setStatus(IN_PROGRESS);
    return buildRepository.save(build);
  }

  public Build createQueued(final Project project) {
    final var build = new Build();
    build.setProject(project);
    build.setStartTime(now());
    build.setStatus(QUEUED);
    return buildRepository.save(build);
  }

  public void complete(final Build build, final BuildStatus status, final CharSequence logs) {
    build.setStatus(status);
    build.setLogs(logs == null ? null : logs.toString());
    build.setEndTime(now());
    buildRepository.save(build);
  }

  public void markInProgress(final Build build) {
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);
  }
}
