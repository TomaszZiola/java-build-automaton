package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProjectServiceTest extends BaseUnit {

  @Test
  void givenValidPayload_whenHandleProjectLookup_thenReturnApiResponse() {
    // when
    final var result = projectServiceImpl.handleProjectLookup(payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }

  @Test
  void givenNotExistingProject_whenHandleProjectLookup_thenReturnApiResponse() {
    // given
    when(projectRepository.findByRepositoryName(payload.repository().fullName()))
        .thenReturn(Optional.empty());

    // when
    final var result = projectServiceImpl.handleProjectLookup(payload);

    // then
    assertThat(result.status()).isEqualTo(NOT_FOUND);
    assertThat(result.message())
        .isEqualTo("Project not found for repository: " + payload.repository().fullName());
  }

  @Test
  void givenGetRequest_whenFindAll_thenReturnListOfProjectDetailsDto() {
    // when
    final var result = projectServiceImpl.findAll();

    // then
    assertThat(result.getFirst()).isEqualTo(projectDetailsDto);
  }

  @Test
  void givenExistingProjectId_whenFindDetailsById_thenReturnProjectDetailsDto() {
    // when
    final var result = projectServiceImpl.findDetailsById(projectId);

    // then
    assertThat(result).isEqualTo(projectDetailsDto);
  }

  @Test
  void givenNotExistingProject_whenFindDetailsById_thenReturnProjectDetailsDto() {
    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> projectServiceImpl.findDetailsById(nonExistentProjectId));
  }

  @Test
  void givenExistingProjectId_whenFindProjectBuilds_thenReturnListOfBuildSummaryDto() {
    // when
    final var result = projectServiceImpl.findProjectBuilds(projectId);

    // then
    assertThat(result.getFirst()).isEqualTo(buildSummaryDto);
  }

  @Test
  void givenNotExistingProject_whenFindProjectBuilds_thenThrowProjectNotFoundException() {
    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> projectServiceImpl.findProjectBuilds(nonExistentProjectId));
  }

  @Test
  void givenExistingBuild_whenFindBuildDetailsById_thenReturnBuildDetailsDto() {
    // when
    final var result = projectServiceImpl.findBuildDetailsById(buildId);

    // then
    assertThat(result).isEqualTo(buildDetailsDto);
  }

  @Test
  void givenNotExistingBuild_whenFindBuildDetailsById_thenThrowBuildNotFoundException() {
    // when / then
    assertThrows(
        BuildNotFoundException.class,
        () -> projectServiceImpl.findBuildDetailsById(nonExistentBuildId));
  }
}
