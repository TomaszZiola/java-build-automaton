package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.models.BuildModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildSummaryDtoModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class BuildMapperTest extends BaseUnit {

  @Test
  void givenBuild_whenToSummaryDto_thenMapsAllFields() {
    // when
    final var result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result).isEqualTo(buildSummaryDto);
  }

  @Test
  void givenBuildWithoutEndTime_whenToSummaryDto_thenEndTimeIsNull() {
    // given
    build = BuildModel.inProgress();
    buildSummaryDto = BuildSummaryDtoModel.inProgress();

    // when
    final var result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result).isEqualTo(buildSummaryDto);
  }

  @Test
  void givenBuil_whentoDetailsDto_thenMapsAllFields() {
    // when
    final var result = buildMapperImpl.toDetailsDto(build);

    // then
    assertThat(result).isEqualTo(buildDetailsDto);
  }
}
