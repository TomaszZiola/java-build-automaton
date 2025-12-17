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

  public Build makeInProgress(Project project) {
    var build = new Build();
    build.setProject(project);
    build.setStartTime(now());
    build.setStatus(IN_PROGRESS);
    return buildRepository.save(build);
  }

  public Build createQueued(Project project) {
    var build = new Build();
    build.setProject(project);
    build.setStartTime(now());
    build.setStatus(QUEUED);
    return buildRepository.save(build);
  }

  public void complete(Build build, BuildStatus status, CharSequence logs) {
    build.setStatus(status);
    build.setLogs(logs == null ? null : logs.toString());
    build.setEndTime(now());
    buildRepository.save(build);
  }

  public void markInProgress(Build build) {
    build.setStatus(IN_PROGRESS);
    buildRepository.save(build);
  }
}
