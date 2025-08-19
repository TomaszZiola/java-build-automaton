package io.github.tomaszziola.javabuildautomaton.utils;

import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter;
import io.github.tomaszziola.javabuildautomaton.models.ApiResponseModel;
import io.github.tomaszziola.javabuildautomaton.models.GitHubWebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookController;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import java.io.File;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
@SuppressWarnings("PMD.TooManyFields")
public class BaseUnit {

  @TempDir protected File tempDir;

  @Mock protected BuildService buildService;
  @Mock protected ProjectRepository projectRepository;
  @Mock protected ProjectService projectService;

  protected BuildService buildServiceImpl;
  protected CorrelationIdFilter filterImpl;
  protected MockHttpServletRequest request;
  protected MockHttpServletResponse response;
  protected ProjectService projectServiceImpl;
  protected WebhookController webhookControllerImpl;

  protected ApiResponse apiResponse;
  protected GitHubWebhookPayload payload;
  protected Project project;

  protected String incomingId = "123e4567-e89b-12d3-a456-426614174000";
  protected String nonExistentPath = new File(tempDir, "does-not-exist").getAbsolutePath();
  protected String repositoryName = "TomaszZiola/test";

  @BeforeEach
  void mockResponses() {
    buildServiceImpl = new BuildService();
    filterImpl = new CorrelationIdFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    projectServiceImpl = new ProjectService(projectRepository, buildService);
    webhookControllerImpl = new WebhookController(projectService);

    apiResponse = ApiResponseModel.basic();
    payload = GitHubWebhookPayloadModel.basic();
    project = ProjectModel.basic();

    when(projectRepository.findByRepositoryName(repositoryName)).thenReturn(Optional.of(project));
    when(projectService.handleProjectLookup(payload)).thenReturn(apiResponse);
  }
}
