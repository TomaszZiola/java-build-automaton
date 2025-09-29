package io.github.tomaszziola.javabuildautomaton.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.util.List.of;
import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.api.dto.BuildDetailsDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildExecutor;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildLifecycleService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildOrchestrator;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildProperties;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildQueueService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;
import io.github.tomaszziola.javabuildautomaton.buildsystem.GitCommandRunner;
import io.github.tomaszziola.javabuildautomaton.buildsystem.OutputCollector;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ProcessExecutor;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ProcessRunner;
import io.github.tomaszziola.javabuildautomaton.buildsystem.ValidationResult;
import io.github.tomaszziola.javabuildautomaton.buildsystem.WorkingDirectoryValidator;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter;
import io.github.tomaszziola.javabuildautomaton.models.ApiResponseModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildDetailsDtoModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildPropertiesModel;
import io.github.tomaszziola.javabuildautomaton.models.BuildSummaryDtoModel;
import io.github.tomaszziola.javabuildautomaton.models.ExecutionResultModel;
import io.github.tomaszziola.javabuildautomaton.models.GitHubWebhookPayloadModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectDetailsDtoModel;
import io.github.tomaszziola.javabuildautomaton.models.ProjectModel;
import io.github.tomaszziola.javabuildautomaton.models.WebhookPropertiesModel;
import io.github.tomaszziola.javabuildautomaton.project.ProjectApiController;
import io.github.tomaszziola.javabuildautomaton.project.ProjectMapper;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.webhook.BranchPolicy;
import io.github.tomaszziola.javabuildautomaton.webhook.IdempotencyService;
import io.github.tomaszziola.javabuildautomaton.webhook.IngestionGuard;
import io.github.tomaszziola.javabuildautomaton.webhook.RequestHeaderAccessor;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookController;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookDeliveryRepository;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookIngestionService;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookProperties;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookSecurityService;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookSignatureFilter;
import io.github.tomaszziola.javabuildautomaton.webhook.WebhookStartupVerifier;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import io.github.tomaszziola.javabuildautomaton.webui.WebUiController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
@SuppressWarnings({
  "PMD.TooManyFields",
  "PMD.CouplingBetweenObjects",
  "PMD.NcssCount",
  "PMD.FieldNamingConventions"
})
public class BaseUnit {

  @TempDir protected File tempDir;

  @Mock protected BranchPolicy branchPolicy;
  @Mock protected BuildExecutor buildExecutor;
  @Mock protected BuildLifecycleService buildLifecycleService;
  @Mock protected BuildMapper buildMapper;
  @Mock protected BuildOrchestrator buildOrchestrator;
  @Mock protected BuildQueueService buildQueueService;
  @Mock protected BuildRepository buildRepository;
  @Mock protected BuildService buildService;
  @Mock protected FilterChain filterChain;
  @Mock protected GitCommandRunner gitCommandRunner;
  @Mock protected HttpServletRequest httpServletRequest;
  @Mock protected IdempotencyService idempotencyService;
  @Mock protected IngestionGuard ingestionGuard;
  @Mock protected Process process;
  @Mock protected ProcessExecutor processExecutor;
  @Mock protected ProcessRunner processRunner;
  @Mock protected ProjectMapper projectMapper;
  @Mock protected ProjectRepository projectRepository;
  @Mock protected ProjectService projectService;
  @Mock protected RequestHeaderAccessor requestHeaderAccessor;
  @Mock protected WebhookDeliveryRepository webhookDeliveryRepository;
  @Mock protected WebhookIngestionService webhookIngestionService;
  @Mock protected WebhookSecurityService webhookSecurityService;
  @Mock protected WorkingDirectoryValidator workingDirectoryValidator;

  protected ArgumentCaptor<Build> buildCaptor;
  protected BranchPolicy branchPolicyImpl;
  protected BuildExecutor buildExecutorImpl;
  protected BuildLifecycleService buildLifecycleServiceImpl;
  protected BuildMapper buildMapperImpl;
  protected BuildOrchestrator buildOrchestratorImpl;
  protected BuildQueueService buildQueueServiceImpl;
  protected BuildService buildServiceImpl;
  protected CorrelationIdFilter correlationIdFilterImpl;
  protected GitCommandRunner gitCommandRunnerImpl;
  protected IdempotencyService idempotencyServiceImpl;
  protected IngestionGuard ingestionGuardImpl;
  protected Model modelImpl;
  protected MockHttpServletRequest httpServletRequestImpl;
  protected MockHttpServletResponse httpServletResponseImpl;
  protected ProcessExecutor processExecutorImpl;
  protected ProcessRunner processRunnerImpl;
  protected ProjectApiController projectApiControllerImpl;
  protected ProjectMapper projectMapperImpl;
  protected ProjectService projectServiceImpl;
  protected RequestHeaderAccessor requestHeaderAccessorImpl;
  protected WebhookController webhookControllerImpl;
  protected WebUiController webUiControllerImpl;
  protected WebhookIngestionService webhookIngestionServiceImpl;
  protected WebhookSignatureFilter webhookSignatureFilterImpl;
  protected WebhookSecurityService webhookSecurityServiceImpl;
  protected WebhookStartupVerifier webhookStartupVerifierImpl;
  protected WebhookProperties webhookConfiguration;

  protected ApiResponse apiResponse;
  protected Build build;
  protected BuildDetailsDto buildDetailsDto;
  protected BuildProperties buildProperties;
  protected BuildSummaryDto buildSummaryDto;
  protected ExecutionResult buildExecutionResult;
  protected ExecutionResult cloneExecutionResult;
  protected ExecutionResult pullExecutionResult;
  protected GitHubWebhookPayload payload;
  protected Project project;
  protected ProjectDetailsDto projectDetailsDto;
  protected WebhookProperties webhookProperties;
  protected File workingDir;

  protected String API_PATH = "/api/projects";
  protected String bodyJson = "{\"msg\":\"hi\"}";
  protected byte[] bodyBytes = bodyJson.getBytes(UTF_8);
  protected Long buildId = 1L;
  protected String[] cmd = {"git", "pull"};
  protected String expectedHex = "447455f04bc3e4c84f552ab236138532bece9ec6e47e813d8e1fd42094bb544e";
  protected String invalidSha256HeaderValue = "sha256=xD";
  protected String incomingId = "123e4567-e89b-12d3-a456-426614174000";
  protected String mainBranch = "refs/heads/main";
  protected String masterBranch = "refs/heads/master";
  protected Long projectId = 1L;
  protected Long nonExistentProjectId = 9L;
  protected Long nonExistentBuildId = 9L;
  protected String repositoryName = "TomaszZiola/test";
  protected String postMethod = "POST";
  protected String temp_prefix = "ws-";
  protected String validSha256HeaderValue = "sha256=" + expectedHex;
  protected String validSha256HeaderName = "X-Hub-Signature-256";
  protected String WEBHOOK_PATH = "/webhook";

  @BeforeEach
  void mockResponses() throws IOException {

    apiResponse = ApiResponseModel.basic();
    build = BuildModel.basic();
    buildDetailsDto = BuildDetailsDtoModel.basic();
    buildExecutionResult = ExecutionResultModel.basic("build");
    buildProperties = BuildPropertiesModel.basic();
    buildSummaryDto = BuildSummaryDtoModel.basic();
    cloneExecutionResult = ExecutionResultModel.basic("clone");
    payload = GitHubWebhookPayloadModel.basic();
    project = ProjectModel.basic();
    projectDetailsDto = ProjectDetailsDtoModel.basic();
    pullExecutionResult = ExecutionResultModel.basic("pull");
    webhookProperties = WebhookPropertiesModel.basic();
    workingDir = createTempDirectory(temp_prefix).toFile();

    buildCaptor = ArgumentCaptor.forClass(Build.class);
    branchPolicyImpl = new BranchPolicy();
    buildExecutorImpl = new BuildExecutor(processExecutor);
    buildLifecycleServiceImpl = new BuildLifecycleService(buildRepository);
    buildMapperImpl = new BuildMapper();
    buildOrchestratorImpl = new BuildOrchestrator(buildQueueService, buildService);
    buildQueueServiceImpl = new BuildQueueService(buildService, buildProperties);
    buildServiceImpl =
        new BuildService(
            buildExecutor,
            buildLifecycleService,
            buildRepository,
            gitCommandRunner,
            workingDirectoryValidator);
    correlationIdFilterImpl = new CorrelationIdFilter();
    gitCommandRunnerImpl = new GitCommandRunner(processExecutor);
    httpServletRequestImpl = new MockHttpServletRequest();
    httpServletResponseImpl = new MockHttpServletResponse();
    idempotencyServiceImpl = new IdempotencyService(webhookDeliveryRepository);
    ingestionGuardImpl =
        new IngestionGuard(branchPolicy, idempotencyService, requestHeaderAccessor);
    modelImpl = new ExtendedModelMap();
    processExecutorImpl = new ProcessExecutor(processRunner, new OutputCollector());
    processRunnerImpl = new ProcessRunner();
    projectApiControllerImpl = new ProjectApiController(projectService);
    projectMapperImpl = new ProjectMapper();
    projectServiceImpl =
        new ProjectService(buildMapper, buildRepository, projectMapper, projectRepository);
    requestHeaderAccessorImpl = new RequestHeaderAccessor();
    webhookControllerImpl = new WebhookController(webhookIngestionService);
    webhookIngestionServiceImpl =
        new WebhookIngestionService(buildOrchestrator, ingestionGuard, projectRepository);
    webhookConfiguration = new WebhookProperties();
    webhookSecurityServiceImpl = new WebhookSecurityService(webhookProperties);
    webhookSignatureFilterImpl = new WebhookSignatureFilter(webhookSecurityService);
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    webUiControllerImpl = new WebUiController(projectService);

    when(branchPolicy.isNonTriggerRef(mainBranch)).thenReturn(false);
    when(buildExecutor.build(project.getBuildTool(), workingDir))
        .thenReturn(buildExecutionResult)
        .thenReturn(buildExecutionResult);
    when(buildLifecycleService.createInProgress(project)).thenReturn(build);
    when(buildMapper.toSummaryDto(build)).thenReturn(buildSummaryDto);
    when(buildMapper.toDetailsDto(build)).thenReturn(buildDetailsDto);
    when(buildRepository.findById(buildId)).thenReturn(Optional.of(build));
    when(buildRepository.findById(nonExistentBuildId)).thenReturn(empty());
    when(buildRepository.findByProject(project)).thenReturn(of(build));
    when(buildRepository.save(any(Build.class))).thenAnswer(inv -> inv.getArgument(0));
    when(buildService.createQueuedBuild(project)).thenReturn(build);
    when(gitCommandRunner.clone(project.getRepositoryUrl(), workingDir))
        .thenReturn(cloneExecutionResult);
    when(gitCommandRunner.pull(workingDir)).thenReturn(pullExecutionResult);
    when(gitCommandRunner.clone(project.getRepositoryUrl(), workingDir))
        .thenReturn(cloneExecutionResult);
    when(httpServletRequest.getRequestURI()).thenReturn("/api/projects/123");
    when(idempotencyService.isDuplicate("id")).thenReturn(false);
    when(processExecutor.execute(tempDir, "mvn", "clean", "install"))
        .thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "gradle", "clean", "build"))
        .thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "git", "pull")).thenReturn(pullExecutionResult);
    when(processExecutor.execute(tempDir, "git", "clone", project.getRepositoryUrl(), "."))
        .thenReturn(cloneExecutionResult);
    when(processRunner.start(workingDir, cmd)).thenReturn(process);
    when(projectMapper.toDetailsDto(project)).thenReturn(projectDetailsDto);
    when(projectRepository.findAll()).thenReturn(of(project));
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectRepository.findById(nonExistentProjectId)).thenReturn(empty());
    when(projectRepository.findByRepositoryName(repositoryName)).thenReturn(Optional.of(project));
    when(projectService.findAll()).thenReturn(of(projectDetailsDto));
    when(projectService.findBuildDetailsById(buildId)).thenReturn(buildDetailsDto);
    when(projectService.findBuildDetailsById(nonExistentBuildId))
        .thenThrow(new BuildNotFoundException(nonExistentBuildId));
    when(projectService.findDetailsById(projectId)).thenReturn(projectDetailsDto);
    when(projectService.findDetailsById(nonExistentProjectId))
        .thenThrow(new ProjectNotFoundException(nonExistentProjectId));
    when(projectService.findProjectBuilds(projectId)).thenReturn(of(buildSummaryDto));
    when(projectService.findProjectBuilds(nonExistentProjectId))
        .thenThrow(ProjectNotFoundException.class);
    when(requestHeaderAccessor.deliveryId()).thenReturn("id");
    when(webhookSecurityService.isSignatureValid(validSha256HeaderValue, bodyBytes))
        .thenReturn(true);
    when(webhookIngestionService.handleWebhook(payload)).thenReturn(apiResponse);
    when(workingDirectoryValidator.prepareWorkspace(
            eq(project), eq(build), isA(StringBuilder.class)))
        .thenReturn(new ValidationResult(true, workingDir));

    RequestContextHolder.resetRequestAttributes();
  }
}
