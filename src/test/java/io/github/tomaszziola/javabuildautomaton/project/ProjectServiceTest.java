package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

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
}
