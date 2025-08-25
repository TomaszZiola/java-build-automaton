package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectServiceTest extends BaseUnit {

  @Test
  @DisplayName("Given valid payload, when handling project lookup, then return ApiResponse")
  void returnsApiResponseWhenPayloadValid() {
    // when
    final var result = projectServiceImpl.handleProjectLookup(payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }

  @Test
  @DisplayName(
      "Given non-existing project, when handling project lookup, then return NOT_FOUND ApiResponse")
  void returnsNotFoundWhenProjectMissingOnLookup() {
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
  @DisplayName("Given request, when finding all, then return list of ProjectDetailsDto")
  void returnsProjectsWhenFindAll() {
    // when
    final var result = projectServiceImpl.findAll();

    // then
    assertThat(result.getFirst()).isEqualTo(projectDetailsDto);
  }

  @Test
  @DisplayName("Given existing project id, when finding details, then return ProjectDetailsDto")
  void returnsProjectDetailsWhenFindDetailsById() {
    // when
    final var result = projectServiceImpl.findDetailsById(projectId);

    // then
    assertThat(result).isEqualTo(projectDetailsDto);
  }

  @Test
  @DisplayName(
      "Given non-existing project id, when finding details, then throw ProjectNotFoundException")
  void throwsWhenProjectMissingOnFindDetailsById() {
    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> projectServiceImpl.findDetailsById(nonExistentProjectId));
  }

  @Test
  @DisplayName(
      "Given existing project id, when finding project builds, then return list of BuildSummaryDto")
  void returnsBuildSummariesWhenFindProjectBuilds() {
    // when
    final var result = projectServiceImpl.findProjectBuilds(projectId);

    // then
    assertThat(result.getFirst()).isEqualTo(buildSummaryDto);
  }

  @Test
  @DisplayName(
      "Given non-existing project id, when finding project builds, then throw ProjectNotFoundException")
  void throwsWhenProjectMissingOnFindProjectBuilds() {
    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> projectServiceImpl.findProjectBuilds(nonExistentProjectId));
  }

  @Test
  @DisplayName("Given existing build id, when finding build details, then return BuildDetailsDto")
  void returnsBuildDetailsWhenFindBuildDetailsById() {
    // when
    final var result = projectServiceImpl.findBuildDetailsById(buildId);

    // then
    assertThat(result).isEqualTo(buildDetailsDto);
  }

  @Test
  @DisplayName(
      "Given non-existing build id, when finding build details, then throw BuildNotFoundException")
  void throwsWhenBuildMissingOnFindBuildDetailsById() {
    // when / then
    assertThrows(
        BuildNotFoundException.class,
        () -> projectServiceImpl.findBuildDetailsById(nonExistentBuildId));
  }
}
