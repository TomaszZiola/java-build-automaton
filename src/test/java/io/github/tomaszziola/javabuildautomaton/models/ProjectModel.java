package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.build.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.project.Project;

public final class ProjectModel {

  private ProjectModel() {}

  public static Project basic() {
    return new Project(
        1L,
        "test-project-from-db",
        "TomaszZiola/test",
        "/Users/Tomasz/Documents/IdeaProjects/test",
        GRADLE);
  }
}
