package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class ProjectMapperTest extends BaseUnit {

  @Test
  void givenProject_whenToDetailsDto_thenMapsAllFields() {
    // when
    final ProjectDetailsDto result = projectMapperImpl.toDetailsDto(project);

    // then
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("test-project-from-db");
    assertThat(result.repositoryName()).isEqualTo("TomaszZiola/test");
    assertThat(result.localPath()).isEqualTo(tempDir.getAbsolutePath());
    assertThat(result.buildTool()).isEqualTo(GRADLE);
  }
}
