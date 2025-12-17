package io.github.tomaszziola.javabuildautomaton.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectRestControllerTest extends BaseUnit {

  @Test
  @DisplayName("Given existing projects, when getting all, then return mapped list")
  void returnsMappedListWhenGetAllProjects() {
    // when
    final var result = projectRestControllerImpl.getAllProjects();

    // then
    assertThat(result).hasSize(1);
    final var first = result.getFirst();
    assertThat(first).isEqualTo(projectDto);
  }

  @Test
  @DisplayName("Given project with builds, when getting builds, then return mapped builds")
  void returnsMappedBuildsWhenGetProjectBuilds() {
    // when
    final var result = projectRestControllerImpl.getProjectBuilds(project.getId());

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(buildSummaryDto);
  }

  @Test
  @DisplayName("Given non-existing project, when getting builds, then throw ProjectNotFound")
  void givenNonExistingProject_whenGetProjectBuilds_thenThrowProjectNotFound() {
    // given
    final var missingId = 9L;

    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> projectRestControllerImpl.getProjectBuilds(missingId));
  }

  @Test
  @DisplayName("Given valid PostProjectDto, when saving, then return ProjectDto")
  void givenPostProjectDto_whenSaveProject_thenReturnProjectDto() {
    // when
    final var result = projectRestControllerImpl.createProject(postProjectDto);

    // then
    assertThat(result).isEqualTo(projectDto);
  }
}
