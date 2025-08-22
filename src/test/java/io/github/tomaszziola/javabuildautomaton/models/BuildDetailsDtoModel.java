package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildDetailsDto;

public final class BuildDetailsDtoModel {

  private BuildDetailsDtoModel() {}

  public static BuildDetailsDto basic() {
    return new BuildDetailsDto(
        42L,
        SUCCESS,
        parse("2025-08-22T11:10:10Z"),
        parse("2025-08-22T11:20:10Z"),
        "Everything worked good!");
  }
}
