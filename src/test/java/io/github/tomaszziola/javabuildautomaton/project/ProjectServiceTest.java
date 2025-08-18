package io.github.tomaszziola.javabuildautomaton.project;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class ProjectServiceTest extends BaseUnit {

  @Test
  void givenValidPayload_whenHandleProjectLookup_thenReturnApiResponse() {
    // when
    final var result = projectServiceImpl.handleProjectLookup(payload);

    // then
    assertThat(result).isEqualTo(apiResponse);
  }
}
