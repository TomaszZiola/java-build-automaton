package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;

public final class ProjectModel {

  private ProjectModel() {}

  public static Project basic(final String localPath) {
    final var project = new Project();
    project.setId(1L);
    project.setName("test-project-from-db");
    project.setLocalPath(localPath);
    project.setRepositoryName("TomaszZiola/test");
    project.setBuildTool(GRADLE);
    project.setCreatedAt(parse("2025-08-24T10:15:10Z"));
    project.setUpdatedAt(parse("2025-09-21T20:10:50Z"));
    return project;
  }
}
