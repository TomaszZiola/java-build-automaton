package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.buildsystem.Build;

public final class BuildModel {

  private BuildModel() {}

  public static Build basic() {
    final var build = new Build();
    build.setId(42L);
    build.setStatus(SUCCESS);
    build.setStartTime(parse("2025-08-22T11:10:10Z"));
    build.setEndTime(parse("2025-08-22T11:20:10Z"));
    build.setLogs("Everything worked good!");
    return build;
  }

  public static Build inProgress() {
    final var build = basic();
    build.setStatus(IN_PROGRESS);
    build.setEndTime(null);
    return build;
  }
}
