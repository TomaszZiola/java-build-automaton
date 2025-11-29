package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion.JAVA_21;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;

public final class PostProjectDtoModel {

  private PostProjectDtoModel() {}

  public static PostProjectDto basic() {
    var postProjectDto = new PostProjectDto();
    postProjectDto.setBuildTool(GRADLE);
    postProjectDto.setJavaVersion(JAVA_21);
    postProjectDto.setRepositoryUrl("https://github.com/TomaszZiola/java-build-automaton.git");
    postProjectDto.setWebhookSecret("secret");
    return postProjectDto;
  }
}
