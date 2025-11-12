package io.github.tomaszziola.javabuildautomaton.project;

import static java.time.Instant.now;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

  private static final String GITHUB_BASE_URL = "https://github.com/";

  public ProjectDto toDetailsDto(Project project) {
    return new ProjectDto(
        project.getId(),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getUsername(),
        project.getRepositoryName(),
        project.getRepositoryFullName(),
        project.getRepositoryUrl(),
        project.getBuildTool(),
        project.getJavaVersion());
  }

  public Project toEntity(PostProjectDto request) {
    Project project = new Project();
    project.setCreatedAt(now());
    project.setRepositoryName(extractRepositoryName(request.getRepositoryUrl()));
    project.setUsername(extractUsername(request.getRepositoryUrl()));
    project.setRepositoryFullName(extractUserAndRepo(request.getRepositoryUrl()));
    project.setRepositoryUrl(request.getRepositoryUrl());
    project.setBuildTool(request.getBuildTool());
    project.setJavaVersion(request.getJavaVersion());
    return project;
  }

  private static String extractUsername(String url) {
    var parts = url.replace(GITHUB_BASE_URL, "").split("/");
    return parts.length >= 2 ? parts[0] : null;
  }

  private static String extractRepositoryName(String url) {
    var parts = url.replace(GITHUB_BASE_URL, "").split("/");
    var repo = parts[1];
    return repo.endsWith(".git") ? repo.substring(0, repo.length() - 4) : repo;
  }

  public static String extractUserAndRepo(String url) {
    var trimmed = url.substring(GITHUB_BASE_URL.length());
    if (trimmed.endsWith(".git")) {
      trimmed = trimmed.substring(0, trimmed.length() - 4);
    }
    return trimmed;
  }
}
