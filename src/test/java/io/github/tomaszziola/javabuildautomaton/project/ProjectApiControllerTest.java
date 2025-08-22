package io.github.tomaszziola.javabuildautomaton.project;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class ProjectApiControllerTest extends BaseUnit {

  @Test
  void givenExistingProjects_whenGetAllProjects_thenReturnMappedList() {
    // when
    final var result = projectApiControllerImpl.getAllProjects();

    // then
    assertThat(result.size()).isEqualTo(1);
    final ProjectDetailsDto first = result.getFirst();
    assertThat(first).isEqualTo(projectDetailsDto);
  }

  @Test
  void givenProjectWithBuilds_whenGetProjectBuilds_thenReturnMappedBuilds() {
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
