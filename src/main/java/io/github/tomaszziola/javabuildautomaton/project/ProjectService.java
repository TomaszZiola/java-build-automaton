package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

  public ProjectService(final ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  public ApiResponse handleProjectLookup(final GitHubWebhookPayload payload) {
    final String repositoryName = payload.repositoryInfo().fullName();

    final Optional<Project> projectOptional =
        projectRepository.findByRepositoryName(repositoryName);

    if (projectOptional.isPresent()) {
      final Project foundProject = projectOptional.get();
      final String message = "Project found in the database: " + foundProject.getName();
      LOGGER.info(">>> {}", message);
      return new ApiResponse("success", message);
    } else {
      final String message = "Project not found for repository: " + repositoryName;
      LOGGER.info(">>> {}", message);
      return new ApiResponse("not_found", message);
    }
  }
}
