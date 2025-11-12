package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PostProjectDto {
  @Pattern(
      regexp = "^https://github\\.com/[^/]+/[^/]+\\.git$",
      message = "Repository URL must match pattern: https://github.com/{user}/{repo}.git")
  private String repositoryUrl;

  @NotNull private BuildTool buildTool;

  private ProjectJavaVersion javaVersion;
}
