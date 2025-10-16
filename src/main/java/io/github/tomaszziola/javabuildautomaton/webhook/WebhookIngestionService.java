package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildOrchestrator;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayloadWithHeaders;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookIngestionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookIngestionService.class);
  private final BuildOrchestrator buildOrchestrator;
  private final IngestionGuard ingestionGuard;
  private final ProjectRepository projectRepository;

  public ApiResponse handleWebhook(WebhookPayloadWithHeaders payload) {
    var ingestionGuardResult = ingestionGuard.evaluateIngestion(payload);
    switch (ingestionGuardResult) {
      case DUPLICATE -> {
        var msg = "Duplicate delivery ignored";
        LOGGER.info(msg);
        return new ApiResponse(NOT_FOUND, msg);
      }
      case NON_TRIGGER_REF -> {
        var msg = "Ignoring event for ref: " + payload.dto().ref();
        LOGGER.info(msg);
        return new ApiResponse(NOT_FOUND, msg);
      }
      case ALLOW -> {
        var repositoryFullName = payload.dto().repository().fullName();
        return projectRepository
            .findByRepositoryFullName(repositoryFullName)
            .map(this::handleProjectFound)
            .orElseGet(() -> handleProjectMissing(repositoryFullName));
      }
      default -> {
        var msg = "Unhandled outcome: " + ingestionGuardResult;
        LOGGER.warn(msg);
        return new ApiResponse(NOT_FOUND, msg);
      }
    }
  }

  private ApiResponse handleProjectFound(Project project) {
    var message = "Project found in the database: " + project.getRepositoryName();
    LOGGER.info(message);
    buildOrchestrator.enqueueBuild(project);
    return new ApiResponse(FOUND, message + ". Build process started.");
  }

  private ApiResponse handleProjectMissing(String repositoryFullName) {
    var message = "Project not found for repositoryFullName: " + repositoryFullName;
    LOGGER.warn(message);
    return new ApiResponse(NOT_FOUND, message);
  }
}
