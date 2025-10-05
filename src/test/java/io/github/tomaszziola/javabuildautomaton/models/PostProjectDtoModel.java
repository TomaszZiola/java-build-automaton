package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;

public final class PostProjectDtoModel {

  private PostProjectDtoModel() {}

  public static PostProjectDto basic() {
    return new PostProjectDto("https://github.com/TomaszZiola/java-build-automaton.git", GRADLE);
  }
}
