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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

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
            .map(this::handleProject)
            .orElseGet(() -> handleProjectMissing(repositoryFullName));
      }
    };
  }

  private ApiResponse respondAndLog(String message) {
    log.info(message);
    return new ApiResponse(SKIPPED, message);
  }

  private ApiResponse handleProject(Project project) {
    var message = "Project found in the database: " + project.getRepositoryName();
    log.info(message);
    buildOrchestrator.enqueue(project);
    return new ApiResponse(FOUND, message + ". Build process started.");
  }

  private ApiResponse handleProjectMissing(String repositoryFullName) {
    var message = "Project not found for repositoryFullName: " + repositoryFullName;
    log.warn(message);
    return new ApiResponse(NOT_FOUND, message);
  }
}
