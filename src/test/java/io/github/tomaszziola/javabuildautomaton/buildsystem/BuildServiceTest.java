package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.tomaszziola.javabuildautomaton.exception.BuildProcessException;
import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class BuildServiceTest extends BaseUnit {
  @Test
  void givenExistingWorkingDirectory_whenStartBuildProcess_thenDoesNotThrow() {
    // when / then
    assertDoesNotThrow(() -> buildService.startBuildProcess(project));
  }

  @Test
  void
      givenNonExistingWorkingDirectory_whenStartBuildProcessWithMaven_thenThrowBuildProcessException() {
    // given
    project = ProjectModel.basic(MAVEN, nonExistentPath);

    // when
    final BuildProcessException exception =
        assertThrows(
            BuildProcessException.class, () -> buildServiceImpl.startBuildProcess(project));

    // then
    assertThat(exception.getMessage()).contains("Failed to execute build command");
  }

  @Test
  void
      givenNonExistingWorkingDirectory_whenStartBuildProcessWithGradle_thenThrowBuildProcessException() {
    // given
    project = ProjectModel.basic(GRADLE, nonExistentPath);

    // when
    final BuildProcessException exception =
        assertThrows(
            BuildProcessException.class, () -> buildServiceImpl.startBuildProcess(project));

    // then
    assertThat(exception.getMessage()).contains("Failed to execute build command");
  }
}
