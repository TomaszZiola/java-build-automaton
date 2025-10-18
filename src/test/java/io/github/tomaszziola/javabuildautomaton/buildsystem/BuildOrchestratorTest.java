package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuildOrchestratorTest extends BaseUnit {

  @Test
  @DisplayName("Given project, when enqueuing build, then create queued build and enqueue id")
  void enqueuesQueuedBuildId() {
    // when & then
    buildOrchestratorImpl.enqueue(project);
    verify(buildLifecycleService).createQueued(project);
    verify(buildQueueService).enqueue(build.getId());
  }
}
