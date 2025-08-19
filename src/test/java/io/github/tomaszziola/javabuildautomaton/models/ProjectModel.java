package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.Project;

public final class ProjectModel {

  private ProjectModel() {}

  public static Project basic() {
    return basic(GRADLE, "/Users/Tomasz/Documents/IdeaProjects/test");
  }

  public static Project basic(final BuildTool buildTool, final String localPath) {
    return new Project(1L, "test-project-from-db", "TomaszZiola/test", localPath, buildTool);
  }
}
