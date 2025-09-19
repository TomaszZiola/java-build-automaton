package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.DUPLICATE;
import static io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard.Outcome.NON_TRIGGER_REF;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildOrchestrator;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookIngestionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookIngestionService.class);
  private final BuildOrchestrator buildOrchestrator;
  private final IngestionGuard ingestionGuard;
  private final ProjectRepository projectRepository;

  public WebhookIngestionService(
      final BuildOrchestrator buildOrchestrator,
      final IngestionGuard ingestionGuard,
      final ProjectRepository projectRepository) {
    this.buildOrchestrator = buildOrchestrator;
    this.ingestionGuard = ingestionGuard;
    this.projectRepository = projectRepository;
  }

  public ApiResponse handleWebhook(final GitHubWebhookPayload payload) {
    final var outcome = ingestionGuard.check(payload.ref());
    if (outcome == DUPLICATE) {
      final var msg = "Duplicate delivery ignored";
      LOGGER.info(msg);
      return new ApiResponse(NOT_FOUND, msg);
    }
    if (outcome == NON_TRIGGER_REF) {
      final var msg = "Ignoring event for ref: " + payload.ref();
      LOGGER.info(msg);
      return new ApiResponse(NOT_FOUND, msg);
    }

    final String repository = payload.repository().fullName();
    return projectRepository
        .findByRepositoryName(repository)
        .map(
            p -> {
              final var message = "Project found in the database: " + p.getName();
              LOGGER.info(message);
              buildOrchestrator.enqueueBuild(p);
              return new ApiResponse(FOUND, message + ". Build process started.");
            })
        .orElseGet(
            () -> {
              final var message = "Project not found for repository: " + repository;
              LOGGER.warn(message);
              return new ApiResponse(NOT_FOUND, message);
            });
  }
}
