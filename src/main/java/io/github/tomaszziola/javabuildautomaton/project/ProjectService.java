package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final BuildService buildService;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

  public ProjectService(
      final ProjectRepository projectRepository, final BuildService buildService) {
    this.projectRepository = projectRepository;
    this.buildService = buildService;
  }

  public ApiResponse handleProjectLookup(final GitHubWebhookPayload payload) {
    final var repositoryName = payload.repository().fullName();

    final var projectOptional = projectRepository.findByRepositoryName(repositoryName);

    if (projectOptional.isPresent()) {
      final var foundProject = projectOptional.get();
      final var message = "Project found in the database: " + foundProject.getName();
      LOGGER.info(message);

      buildService.startBuildProcess(foundProject);

      return new ApiResponse(FOUND, message + ". Build process started.");
    } else {
      final var message = "Project not found for repository: " + repositoryName;
      LOGGER.warn(message);
      return new ApiResponse(NOT_FOUND, message);
    }
  }
}
