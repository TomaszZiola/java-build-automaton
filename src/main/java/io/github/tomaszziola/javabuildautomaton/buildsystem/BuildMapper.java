package io.github.tomaszziola.javabuildautomaton.buildsystem;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import org.springframework.stereotype.Component;

@Component
public class BuildMapper {

  public BuildSummaryDto toSummaryDto(final Build build) {
    return new BuildSummaryDto(
        build.getId(), build.getStatus(), build.getStartTime(), build.getEndTime());
  }
}
