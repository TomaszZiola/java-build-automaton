package io.github.tomaszziola.javabuildautomaton.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildExecutor;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;
import io.github.tomaszziola.javabuildautomaton.buildsystem.GitCommandRunner;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ProcessExecutor;
import io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter;
import io.github.tomaszziola.javabuildautomaton.models.ApiResponseModel;
import io.github.tomaszziola.javabuildautomaton.models.ExecutionResultModel;
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
import org.mockito.ArgumentCaptor;
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

  @Mock protected BuildExecutor buildExecutor;
  @Mock protected BuildRepository buildRepository;
  @Mock protected BuildService buildService;
  @Mock protected GitCommandRunner gitCommandRunner;
  @Mock protected ProcessExecutor processExecutor;
  @Mock protected ProjectRepository projectRepository;
  @Mock protected ProjectService projectService;

  protected ArgumentCaptor<Build> buildCaptor;
  protected BuildExecutor buildExecutorImpl;
  protected BuildService buildServiceImpl;
  protected CorrelationIdFilter filterImpl;
  protected GitCommandRunner gitCommandRunnerImpl;
  protected MockHttpServletRequest request;
  protected MockHttpServletResponse response;
  protected ProjectService projectServiceImpl;
  protected WebhookController webhookControllerImpl;

  protected ApiResponse apiResponse;
  protected ExecutionResult successExecutionResult;
  protected GitHubWebhookPayload payload;
  protected Project project;

  protected String incomingId = "123e4567-e89b-12d3-a456-426614174000";
  protected String input = " \r\nHello\rWorld\n \n";
  protected String nonExistentPath = new File(tempDir, "does-not-exist").getAbsolutePath();
  protected String repositoryName = "TomaszZiola/test";

  @BeforeEach
  void mockResponses() {
    buildCaptor = ArgumentCaptor.forClass(Build.class);
    buildExecutorImpl = new BuildExecutor(processExecutor);
    buildServiceImpl = new BuildService(buildRepository, gitCommandRunner, buildExecutor);
    filterImpl = new CorrelationIdFilter();
    gitCommandRunnerImpl = new GitCommandRunner(processExecutor);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    projectServiceImpl = new ProjectService(projectRepository, buildService);
    webhookControllerImpl = new WebhookController(projectService);

    apiResponse = ApiResponseModel.basic();
    successExecutionResult = ExecutionResultModel.basic();
    payload = GitHubWebhookPayloadModel.basic();
    project = ProjectModel.basic();

    when(buildExecutor.build(any(BuildTool.class), any(File.class)))
        .thenReturn(successExecutionResult);
    when(gitCommandRunner.pull(any(File.class))).thenReturn(successExecutionResult);
    when(processExecutor.execute(tempDir, "mvn", "clean", "install"))
        .thenReturn(successExecutionResult);
    when(processExecutor.execute(tempDir, "gradle", "clean", "build"))
        .thenReturn(successExecutionResult);
    when(processExecutor.execute(tempDir, "git", "pull")).thenReturn(successExecutionResult);
    when(projectRepository.findByRepositoryName(repositoryName)).thenReturn(Optional.of(project));
    when(projectService.handleProjectLookup(payload)).thenReturn(apiResponse);
  }
}
