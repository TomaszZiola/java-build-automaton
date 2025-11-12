package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion.JAVA_21;
import static java.time.Instant.parse;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;

public final class ProjectDetailsDtoModel {

  private ProjectDetailsDtoModel() {}

  public static ProjectDto basic() {
    return new ProjectDto(
        1L,
        parse("2025-08-24T10:15:10Z"),
        parse("2025-09-21T20:10:50Z"),
        "TomaszZiola",
        "java-build-automaton",
        "TomaszZiola/java-build-automaton",
        "https://github.com/TomaszZiola/java-build-automaton.git",
        GRADLE,
        JAVA_21);
  }
}
