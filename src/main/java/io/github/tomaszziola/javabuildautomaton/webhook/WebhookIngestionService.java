package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;

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
    final var ingestionGuardResult = ingestionGuard.check(payload.ref());
    switch (ingestionGuardResult) {
      case DUPLICATE -> {
        final var msg = "Duplicate delivery ignored";
        LOGGER.info(msg);
        return new ApiResponse(NOT_FOUND, msg);
      }
      case NON_TRIGGER_REF -> {
        final var msg = "Ignoring event for ref: " + payload.ref();
        LOGGER.info(msg);
        return new ApiResponse(NOT_FOUND, msg);
      }
      case ALLOW -> {
        final var repository = payload.repository().fullName();
        return projectRepository
            .findByRepositoryName(repository)
            .map(
                project -> {
                  final var message = "Project found in the database: " + project.getName();
                  LOGGER.info(message);
                  buildOrchestrator.enqueueBuild(project);
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
    final var msg = "Unhandled outcome: " + ingestionGuardResult;
    LOGGER.warn(msg);
    return new ApiResponse(NOT_FOUND, msg);
  }
}
