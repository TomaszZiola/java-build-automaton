package io.github.tomaszziola.javabuildautomaton.buildsystem;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuildOrchestrator {

  private final BuildQueueService buildQueueService;
  private final BuildLifecycleService buildLifecycleService;

  public void enqueueBuild(Project project) {
    var queued = buildLifecycleService.createQueued(project);
    buildQueueService.enqueue(queued.getId());
  }
}
