package io.github.tomaszziola.javabuildautomaton.project;

import static java.time.Instant.now;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

  public ProjectDto toDetailsDto(final Project project) {
    return new ProjectDto(
        project.getId(),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getUsername(),
        project.getRepositoryName(),
        project.getFullName(),
        project.getRepositoryUrl(),
        project.getBuildTool());
  }

  public Project toEntity(final PostProjectDto request) {
    final Project project = new Project();
    project.setCreatedAt(now());
    project.setRepositoryName(extractRepositoryName(request.repositoryUrl()));
    project.setUsername(extractUsername(request.repositoryUrl()));
    project.setFullName(extractUserAndRepo(request.repositoryUrl()));
    project.setRepositoryUrl(request.repositoryUrl());
    project.setBuildTool(request.buildTool());
    return project;
  }

  private static String extractUsername(final String url) {
    final var parts = url.replace("https://github.com/", "").split("/");
    return parts.length >= 2 ? parts[0] : null;
  }

  private static String extractRepositoryName(final String url) {
    final var parts = url.replace("https://github.com/", "").split("/");
    final var repo = parts[1];
    return repo.endsWith(".git") ? repo.substring(0, repo.length() - 4) : repo;
  }

  public static String extractUserAndRepo(final String url) {
    var trimmed = url.substring("https://github.com/".length());
    if (trimmed.endsWith(".git")) {
      trimmed = trimmed.substring(0, trimmed.length() - 4);
    }
    return trimmed;
  }
}
