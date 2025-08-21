package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
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
    assertThat(first.id()).isEqualTo(project.getId());
    assertThat(first.name()).isEqualTo(project.getName());
    assertThat(first.repositoryName()).isEqualTo(project.getRepositoryName());
    assertThat(first.localPath()).isEqualTo(project.getLocalPath());
    assertThat(first.buildTool()).isEqualTo(project.getBuildTool());
  }

  @Test
  void givenProjectWithBuilds_whenGetProjectBuilds_thenReturnMappedBuilds() {
    // when
    final var result = projectApiControllerImpl.getProjectBuilds(project.getId());

    // then
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.getFirst().id()).isEqualTo(1L);
    assertThat(result.getFirst().startTime()).isEqualTo(build.getStartTime());
    assertThat(result.getFirst().endTime()).isEqualTo(build.getEndTime());
    assertThat(result.getFirst().status()).isEqualTo(SUCCESS);
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
