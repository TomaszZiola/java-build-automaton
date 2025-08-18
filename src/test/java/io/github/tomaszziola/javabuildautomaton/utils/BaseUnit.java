package io.github.tomaszziola.javabuildautomaton.utils;

import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.build.BuildService;
import io.github.tomaszziola.javabuildautomaton.models.ApiResponseModel;
import io.github.tomaszziola.javabuildautomaton.models.GitHubWebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookController;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class BaseUnit {

  @Mock protected BuildService buildService;
  @Mock protected ProjectRepository projectRepository;
  @Mock protected ProjectService projectService;

  protected BuildService buildServiceImpl;
  protected ProjectService projectServiceImpl;
  protected WebhookController webhookControllerImpl;

  protected ApiResponse apiResponse;
  protected GitHubWebhookPayload payload;
  protected Project project;

  protected String repositoryName = "TomaszZiola/test";

  @BeforeEach
  void mockResponses() {
    buildServiceImpl = new BuildService();
    projectServiceImpl = new ProjectService(projectRepository, buildService);
    webhookControllerImpl = new WebhookController(projectService);

    apiResponse = ApiResponseModel.basic();
    payload = GitHubWebhookPayloadModel.basic();
    project = ProjectModel.basic();

    when(projectRepository.findByRepositoryName(repositoryName)).thenReturn(Optional.of(project));
    when(projectService.handleProjectLookup(payload)).thenReturn(apiResponse);
  }
}
