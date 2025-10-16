package io.github.tomaszziola.javabuildautomaton.project;

import static java.time.Instant.now;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

  public ProjectDto toDetailsDto(Project project) {
    return new ProjectDto(
        project.getId(),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getUsername(),
        project.getRepositoryName(),
        project.getRepositoryFullName(),
        project.getRepositoryUrl(),
        project.getBuildTool());
  }

  public Project toEntity(PostProjectDto request) {
    Project project = new Project();
    project.setCreatedAt(now());
    project.setRepositoryName(extractRepositoryName(request.repositoryUrl()));
    project.setUsername(extractUsername(request.repositoryUrl()));
    project.setRepositoryFullName(extractUserAndRepo(request.repositoryUrl()));
    project.setRepositoryUrl(request.repositoryUrl());
    project.setBuildTool(request.buildTool());
    return project;
  }

  private static String extractUsername(String url) {
    var parts = url.replace("https://github.com/", "").split("/");
    return parts.length >= 2 ? parts[0] : null;
  }

  private static String extractRepositoryName(String url) {
    var parts = url.replace("https://github.com/", "").split("/");
    var repo = parts[1];
    return repo.endsWith(".git") ? repo.substring(0, repo.length() - 4) : repo;
  }

  public static String extractUserAndRepo(String url) {
    var trimmed = url.substring("https://github.com/".length());
    if (trimmed.endsWith(".git")) {
      trimmed = trimmed.substring(0, trimmed.length() - 4);
    }
    return trimmed;
  }
}
