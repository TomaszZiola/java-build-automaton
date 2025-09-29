package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;

public final class ProjectDetailsDtoModel {

  private ProjectDetailsDtoModel() {}

  public static ProjectDetailsDto basic() {
    return new ProjectDetailsDto(
        1L,
        parse("2025-08-24T10:15:10Z"),
        parse("2025-09-21T20:10:50Z"),
        "https://github.com/example/test",
        "test-project-from-db",
        "/IdeaProjects/test",
        "testApp",
        GRADLE);
  }
}
