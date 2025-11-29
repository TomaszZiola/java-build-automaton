package io.github.tomaszziola.javabuildautomaton.project;

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
    var url = request.getRepositoryUrl();
    var project = new Project();
    project.setRepositoryName(extractRepositoryName(url));
    project.setUsername(extractUsername(url));
    project.setRepositoryFullName(extractUserAndRepo(url));
    project.setRepositoryUrl(url);
    project.setBuildTool(request.getBuildTool());
    project.setJavaVersion(request.getJavaVersion());
    project.setWebhookSecret(request.getWebhookSecret());
    return project;
  }

  private static String extractUsername(String url) {
    var path = stripGitHubPrefix(url);
    var parts = path.split("/");
    return parts.length >= 1 ? parts[0] : null;
  }

  private static String extractRepositoryName(String url) {
    var path = stripGitHubPrefix(url);
    var parts = path.split("/");
    if (parts.length < 2) {
      return null;
    }
    return stripGitSuffix(parts[1]);
  }

  public static String extractUserAndRepo(String url) {
    return stripGitSuffix(stripGitHubPrefix(url));
  }

  private static String stripGitHubPrefix(String url) {
    return url.startsWith(GITHUB_BASE_URL)
        ? url.substring(GITHUB_BASE_URL.length())
        : url;
  }

  private static String stripGitSuffix(String path) {
    return path.endsWith(".git")
        ? path.substring(0, path.length() - 4)
        : path;
  }
}
