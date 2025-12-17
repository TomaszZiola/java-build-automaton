package io.github.tomaszziola.javabuildautomaton.buildsystem;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuildOrchestrator {

  private final BuildQueueService buildQueueService;
  private final BuildLifecycleService buildLifecycleService;

  public void enqueue(Project project) {
    var queuedBuild = buildLifecycleService.createQueued(project);
    buildQueueService.enqueue(queuedBuild.getId());
  }
}
