package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static java.time.Instant.parse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectMapperTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given Project entity, when mapping to ProjectDto, then all fields are mapped correctly")
  void givenProjectEntity_whenMappingToProjectDto_thenAllFieldsMappedCorrectly() {
    // when
    final var result = projectMapperImpl.toDetailsDto(project);

    // then
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.username()).isEqualTo("TomaszZiola");
    assertThat(result.repositoryName()).isEqualTo("java-build-automaton");
    assertThat(result.fullName()).isEqualTo("TomaszZiola/java-build-automaton");
    assertThat(result.repositoryUrl())
        .isEqualTo("https://github.com/TomaszZiola/java-build-automaton.git");
    assertThat(result.createdAt()).isEqualTo(parse("2025-08-24T10:15:10Z"));
    assertThat(result.updatedAt()).isEqualTo(parse("2025-09-21T20:10:50Z"));
    assertThat(result.repositoryUrl())
        .isEqualTo("https://github.com/TomaszZiola/java-build-automaton.git");
    assertThat(result.buildTool()).isEqualTo(GRADLE);
  }

  @Test
  @DisplayName(
      "Given PostProjectDto, when mapping to Project entity, then all fields are mapped correctly")
  void givenPostProjectDto_whenMappingToProject_thenAllFieldsMappedCorrectly() {
    // when
    final var result = projectMapperImpl.toEntity(postProjectDto);

    // then
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getRepositoryName()).isEqualTo("java-build-automaton");
    assertThat(result.getUsername()).isEqualTo("TomaszZiola");
    assertThat(result.getRepositoryFullName()).isEqualTo("TomaszZiola/java-build-automaton");
    assertThat(result.getRepositoryUrl())
        .isEqualTo("https://github.com/TomaszZiola/java-build-automaton.git");
    assertThat(result.getBuildTool()).isEqualTo(GRADLE);
  }
}
