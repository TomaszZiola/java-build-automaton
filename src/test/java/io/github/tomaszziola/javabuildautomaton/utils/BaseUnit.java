package io.github.tomaszziola.javabuildautomaton.utils;

import static java.util.List.of;
import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildExecutor;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;
import io.github.tomaszziola.javabuildautomaton.buildsystem.GitCommandRunner;
import io.github.tomaszziola.javabuildautomaton.buildsystem.OutputCollector;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ProcessExecutor;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ProcessRunner;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter;
import io.github.tomaszziola.javabuildautomaton.models.ApiResponseModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildSummaryDtoModel;
import io.github.tomaszziola.javabuildautomaton.models.ExecutionResultModel;
import io.github.tomaszziola.javabuildautomaton.models.GitHubWebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectDetailsDtoModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.project.ProjectApiController;
import io.github.tomaszziola.javabuildautomaton.project.ProjectMapper;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookController;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import java.io.File;
import java.io.IOException;
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
@SuppressWarnings({"PMD.TooManyFields", "PMD.CouplingBetweenObjects"})
public class BaseUnit {

  @TempDir protected File tempDir;

  @Mock protected BuildExecutor buildExecutor;
  @Mock protected BuildMapper buildMapper;
  @Mock protected BuildRepository buildRepository;
  @Mock protected BuildService buildService;
  @Mock protected GitCommandRunner gitCommandRunner;
  @Mock protected Process process;
  @Mock protected ProcessExecutor processExecutor;
  @Mock protected ProcessRunner processRunner;
  @Mock protected ProjectMapper projectMapper;
  @Mock protected ProjectRepository projectRepository;
  @Mock protected ProjectService projectService;

  protected ArgumentCaptor<Build> buildCaptor;
  protected BuildExecutor buildExecutorImpl;
  protected BuildMapper buildMapperImpl;
  protected BuildService buildServiceImpl;
  protected CorrelationIdFilter filterImpl;
  protected GitCommandRunner gitCommandRunnerImpl;
  protected MockHttpServletRequest request;
  protected MockHttpServletResponse response;
  protected ProcessExecutor processExecutorImpl;
  protected ProjectApiController projectApiControllerImpl;
  protected ProjectMapper projectMapperImpl;
  protected ProjectService projectServiceImpl;
  protected WebhookController webhookControllerImpl;

  protected ApiResponse apiResponse;
  protected Build build;
  protected BuildSummaryDto buildSummaryDto;
  protected ExecutionResult buildExecutionResult;
  protected ExecutionResult pullExecutionResult;
  protected File workingDir;
  protected GitHubWebhookPayload payload;
  protected Project project;
  protected ProjectDetailsDto projectDetailsDto;

  protected String[] cmd = {"git", "pull"};
  protected String incomingId = "123e4567-e89b-12d3-a456-426614174000";
  protected String nonExistentPath = new File(tempDir, "does-not-exist").getAbsolutePath();
  protected Long projectId = 1L;
  protected Long nonExistentProjectId = 9L;
  protected String repositoryName = "TomaszZiola/test";

  @BeforeEach
  void mockResponses() throws IOException {
    buildCaptor = ArgumentCaptor.forClass(Build.class);
    buildExecutorImpl = new BuildExecutor(processExecutor);
    buildMapperImpl = new BuildMapper();
    buildServiceImpl = new BuildService(buildRepository, gitCommandRunner, buildExecutor);
    filterImpl = new CorrelationIdFilter();
    gitCommandRunnerImpl = new GitCommandRunner(processExecutor);
    processExecutorImpl = new ProcessExecutor(processRunner, new OutputCollector());
    projectApiControllerImpl = new ProjectApiController(projectService);
    projectMapperImpl = new ProjectMapper();
    projectServiceImpl =
        new ProjectService(
            buildMapper, buildRepository, buildService, projectMapper, projectRepository);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    webhookControllerImpl = new WebhookController(projectService);

    apiResponse = ApiResponseModel.basic();
    build = BuildModel.basic();
    buildExecutionResult = ExecutionResultModel.basic("build");
    buildSummaryDto = BuildSummaryDtoModel.basic();
    pullExecutionResult = ExecutionResultModel.basic();
    payload = GitHubWebhookPayloadModel.basic();
    project = ProjectModel.basic();
    projectDetailsDto = ProjectDetailsDtoModel.basic();
    workingDir = new File(project.getLocalPath());

    when(buildExecutor.build(any(BuildTool.class), any(File.class)))
        .thenReturn(buildExecutionResult);
    when(buildMapper.toSummaryDto(build)).thenReturn(buildSummaryDto);
    when(buildRepository.findByProject(project)).thenReturn(of(build));
    when(gitCommandRunner.pull(workingDir)).thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "mvn", "clean", "install"))
        .thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "gradle", "clean", "build"))
        .thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "git", "pull")).thenReturn(pullExecutionResult);
    when(processRunner.start(workingDir, cmd)).thenReturn(process);
    when(projectMapper.toDetailsDto(project)).thenReturn(projectDetailsDto);
    when(projectRepository.findAll()).thenReturn(of(project));
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectRepository.findById(nonExistentProjectId)).thenReturn(empty());
    when(projectRepository.findByRepositoryName(repositoryName)).thenReturn(Optional.of(project));
    when(projectService.findAll()).thenReturn(of(projectDetailsDto));
    when(projectService.findProjectBuilds(projectId)).thenReturn(of(buildSummaryDto));
    when(projectService.findProjectBuilds(nonExistentProjectId))
        .thenThrow(ProjectNotFoundException.class);
    when(projectService.handleProjectLookup(payload)).thenReturn(apiResponse);
  }
}
