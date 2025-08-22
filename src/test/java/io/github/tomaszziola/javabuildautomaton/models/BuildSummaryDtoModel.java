package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus;

public final class BuildSummaryDtoModel {

  private BuildSummaryDtoModel() {}

  public static BuildSummaryDto basic() {
    return new BuildSummaryDto(
        42L, SUCCESS, parse("2025-08-22T11:10:10Z"), parse("2025-08-22T11:20:10Z"));
  }

  public static BuildSummaryDto basic(final BuildStatus status) {
    return new BuildSummaryDto(42L, status, parse("2025-08-22T11:10:10Z"), null);
  }

  public static BuildSummaryDto inProgress() {
    return basic(IN_PROGRESS);
  }
}
