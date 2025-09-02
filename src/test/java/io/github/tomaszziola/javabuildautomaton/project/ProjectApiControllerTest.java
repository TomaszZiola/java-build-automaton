package io.github.tomaszziola.javabuildautomaton.project;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectApiControllerTest extends BaseUnit {

  @Test
  @DisplayName("Given existing projects, when getting all, then return mapped list")
  void returnsMappedListWhenGetAllProjects() {
    // when
    final var result = projectApiControllerImpl.getAllProjects();

    // then
    assertThat(result.size()).isEqualTo(1);
    final ProjectDetailsDto first = result.getFirst();
    assertThat(first).isEqualTo(projectDetailsDto);
  }

  @Test
  @DisplayName("Given project with builds, when getting builds, then return mapped builds")
  void returnsMappedBuildsWhenGetProjectBuilds() {
    // when
    final var result = projectApiControllerImpl.getProjectBuilds(project.getId());

    // then
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.getFirst()).isEqualTo(buildSummaryDto);
  }

  @Test
  void givenNonExistingProject_whenGetProjectBuilds_thenThrowProjectNotFound() {
    // given
    final long missingId = 9L;

    // when / then
    assertThrows(
        ProjectNotFoundException.class, () -> projectApiControllerImpl.getProjectBuilds(missingId));
  }
}
