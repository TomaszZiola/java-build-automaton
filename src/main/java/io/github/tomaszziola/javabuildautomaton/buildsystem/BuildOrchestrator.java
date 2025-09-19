package io.github.tomaszziola.javabuildautomaton.buildsystem;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class BuildOrchestrator {

  private final BuildQueueService buildQueueService;
  private final BuildService buildService;

  public BuildOrchestrator(
      final BuildQueueService buildQueueService, final BuildService buildService) {
    this.buildQueueService = buildQueueService;
    this.buildService = buildService;
  }

  public void enqueueBuild(final Project project) {
    final var queued = buildService.createQueuedBuild(project);
    buildQueueService.enqueue(queued.getId());
  }
}
