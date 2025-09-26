package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildDetailsDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private final BuildMapper buildMapper;
  private final BuildRepository buildRepository;
  private final ProjectMapper projectMapper;
  private final ProjectRepository projectRepository;

  public ProjectService(
      final BuildMapper buildMapper,
      final BuildRepository buildRepository,
      final ProjectMapper projectMapper,
      final ProjectRepository projectRepository) {
    this.buildMapper = buildMapper;
    this.buildRepository = buildRepository;
    this.projectMapper = projectMapper;
    this.projectRepository = projectRepository;
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

  @Transactional(readOnly = true)
  public List<BuildSummaryDto> findProjectBuilds(final Long projectId) {
    final var project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    final var builds = buildRepository.findByProject(project);
    return builds.stream().map(buildMapper::toSummaryDto).toList();
  }

  public BuildDetailsDto findBuildDetailsById(final Long buildId) {
    return buildRepository
        .findById(buildId)
        .map(buildMapper::toDetailsDto)
        .orElseThrow(() -> new BuildNotFoundException(buildId));
  }
}
