package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.models.BuildModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;

class BuildMapperTest extends BaseUnit {

  @Test
  void givenBuild_whenToSummaryDto_thenMapsAllFields() {
    // when
    final BuildSummaryDto result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result.id()).isEqualTo(42L);
    assertThat(result.status()).isEqualTo(SUCCESS);
    assertThat(result.startTime()).isEqualTo(build.getStartTime());
    assertThat(result.endTime()).isEqualTo(build.getEndTime());
  }

  @Test
  void givenBuildWithoutEndTime_whenToSummaryDto_thenEndTimeIsNull() {
    // given
    build = BuildModel.inProgress();

    // when
    final BuildSummaryDto result = buildMapperImpl.toSummaryDto(build);

    // then
    assertThat(result.id()).isEqualTo(42L);
    assertThat(result.status()).isEqualTo(IN_PROGRESS);
    assertThat(result.startTime()).isEqualTo(build.getStartTime());
    assertThat(result.endTime()).isNull();
  }
}
