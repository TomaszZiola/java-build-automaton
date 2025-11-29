package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion.JAVA_21;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;

public final class ProjectModel {

  private ProjectModel() {}

  public static Project basic() {
    var project = new Project();
    project.setId(1L);
    project.setUsername("TomaszZiola");
    project.setRepositoryName("java-build-automaton");
    project.setRepositoryFullName("TomaszZiola/java-build-automaton");
    project.setRepositoryUrl("https://github.com/TomaszZiola/java-build-automaton.git");
    project.setBuildTool(GRADLE);
    project.setJavaVersion(JAVA_21);
    project.setCreatedAt(parse("2025-08-24T10:15:10Z"));
    project.setUpdatedAt(parse("2025-09-21T20:10:50Z"));
    project.setWebhookSecret("secret");
    return project;
  }

  public static Project unpersisted() {
    var project = new Project();
    project.setUsername("TomaszZiola");
    project.setRepositoryName("java-build-automaton");
    project.setRepositoryFullName("TomaszZiola/java-build-automaton");
    project.setRepositoryUrl("https://github.com/TomaszZiola/java-build-automaton.git");
    project.setBuildTool(GRADLE);
    project.setJavaVersion(JAVA_21);
    return project;
  }
}
