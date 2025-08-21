package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;

public final class ProjectDetailsDtoModel {

  private ProjectDetailsDtoModel() {}

  public static ProjectDetailsDto basic() {
    return new ProjectDetailsDto(
        1L,
        "test-project-from-db",
        "TomaszZiola/test",
        "/Users/Tomasz/Documents/IdeaProjects/test",
        GRADLE);
  }
}
