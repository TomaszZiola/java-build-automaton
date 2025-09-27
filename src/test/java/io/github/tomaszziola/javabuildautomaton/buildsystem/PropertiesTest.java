package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PropertiesTest {

  @Test
  @DisplayName("WorkspaceProperties stores and returns baseDir")
  void workspaceProperties() {
    final WorkspaceProperties props = new WorkspaceProperties();
    props.setBaseDir("/tmp/workspaces");
    assertThat(props.getBaseDir()).isEqualTo("/tmp/workspaces");
  }

  @Test
  @DisplayName("BuildProperties stores and returns values")
  void buildProperties() {
    final BuildProperties props = new BuildProperties();
    props.setMaxParallel(5);
    assertThat(props.getMaxParallel()).isEqualTo(5);

    final BuildProperties.QueueProps queue = props.getQueue();
    assertThat(queue.getCapacity()).isEqualTo(100);
    queue.setCapacity(7);
    assertThat(queue.getCapacity()).isEqualTo(7);
  }
}
