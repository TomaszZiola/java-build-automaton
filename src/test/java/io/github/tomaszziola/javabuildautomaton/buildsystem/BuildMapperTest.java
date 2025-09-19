package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.models.BuildModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildSummaryDtoModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuildMapperTest extends BaseUnit {

  @Test
  @DisplayName("Given Build entity, when mapping to summary DTO, then map all fields")
  void mapsSummaryFieldsWhenBuildProvided() {
    // when
    final var result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result).isEqualTo(buildSummaryDto);
  }

  @Test
  @DisplayName("Given build without end time, when mapping to summary DTO, then endTime is null")
  void mapsSummaryEndTimeNullWhenNoEndTime() {
    // given
    build = BuildModel.inProgress();
    buildSummaryDto = BuildSummaryDtoModel.inProgress();

    // when
    final var result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result).isEqualTo(buildSummaryDto);
  }

  @Test
  @DisplayName("Given Build entity, when mapping to details DTO, then map all fields")
  void mapsDetailsFieldsWhenBuildProvided() {
    // when
    final var result = buildMapperImpl.toDetailsDto(build);

    // then
    assertThat(result).isEqualTo(buildDetailsDto);
  }
}
