package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;
import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

  private final BuildMapper buildMapper;
  private final BuildRepository buildRepository;
  private final BuildService buildService;
  private final ProjectMapper projectMapper;
  private final ProjectRepository projectRepository;

  public ProjectService(
      final BuildMapper buildMapper,
      final BuildRepository buildRepository,
      final BuildService buildService,
      final ProjectMapper projectMapper,
      final ProjectRepository projectRepository) {
    this.buildMapper = buildMapper;
    this.buildRepository = buildRepository;
    this.buildService = buildService;
    this.projectMapper = projectMapper;
    this.projectRepository = projectRepository;
  }

  public ApiResponse handleProjectLookup(final GitHubWebhookPayload payload) {
    final var repositoryName = payload.repository().fullName();

    final var projectOptional = projectRepository.findByRepositoryName(repositoryName);

    if (projectOptional.isPresent()) {
      final var foundProject = projectOptional.get();
      final var message = "Project found in the database: " + foundProject.getName();
      LOGGER.info(message);

      buildService.startBuildProcess(foundProject);

      return new ApiResponse(FOUND, message + ". Build process started.");
    } else {
      final var message = "Project not found for repository: " + repositoryName;
      LOGGER.warn(message);
      return new ApiResponse(NOT_FOUND, message);
    }
  }

  public List<ProjectDetailsDto> findAll() {
    return projectRepository.findAll().stream().map(projectMapper::toDetailsDto).toList();
  }

  public ProjectDetailsDto findDetailsById(final Long projectId) {
    return projectRepository
        .findById(projectId)
        .map(projectMapper::toDetailsDto)
        .orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  public List<BuildSummaryDto> findProjectBuilds(final Long projectId) {
    final Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    final var builds = buildRepository.findByProject(project);
    return builds.stream().map(buildMapper::toSummaryDto).toList();
  }
}
