package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PostProjectDto {

  @NotBlank(message = "Repository URL is required")
  @Pattern(
      regexp = "^https://github\\.com/[^/]+/[^/]+\\.git$",
      message = "Repository URL must match pattern: https://github.com/{user}/{repo}.git")
  private String repositoryUrl;

  @NotNull(message = "Build tool is required")
  private BuildTool buildTool;

  @NotNull(message = "Java version is required")
  private ProjectJavaVersion javaVersion;

  @NotBlank(message = "Webhook secret is required")
  private String webhookSecret;
}
