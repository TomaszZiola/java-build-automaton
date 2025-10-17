package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.SKIPPED;

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
public class WebhookService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);
  private final BuildOrchestrator buildOrchestrator;
  private final IngestionGuard ingestionGuard;
  private final ProjectRepository projectRepository;

  public ApiResponse handle(WebhookPayloadWithHeaders payload) {
    var result = ingestionGuard.evaluate(payload);
    var dto = payload.dto();

    return switch (result) {
      case DUPLICATE ->
          respondAndLog("Duplicate delivery ignored for Deliver ID: " + payload.deliveryId());
      case NON_TRIGGER_REF ->
          respondAndLog("Non triggered ref ignored for Deliver ID: " + payload.deliveryId());
      case ALLOW -> {
        var repositoryFullName = dto.repository().fullName();
        yield projectRepository
            .findByRepositoryFullName(repositoryFullName)
            .map(this::handleProjectFound)
            .orElseGet(() -> handleProjectMissing(repositoryFullName));
      }
    };
  }

  private ApiResponse respondAndLog(String message) {
    LOGGER.info(message);
    return new ApiResponse(SKIPPED, message);
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
