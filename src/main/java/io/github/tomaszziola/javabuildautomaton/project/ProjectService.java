package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final BuildMapper buildMapper;
  private final BuildRepository buildRepository;
  private final ProjectMapper projectMapper;
  private final ProjectRepository projectRepository;

  public List<ProjectDto> findAll() {
    return projectRepository.findAll().stream().map(projectMapper::toDetailsDto).toList();
  }

  public ProjectDto findDetailsById(Long projectId) {
    return projectRepository
        .findById(projectId)
        .map(projectMapper::toDetailsDto)
        .orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  public List<BuildSummaryDto> findProjectBuilds(Long projectId) {
    var project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    var builds = buildRepository.findByProject(project);
    return builds.stream().map(buildMapper::toSummaryDto).toList();
  }

  public ProjectDto saveProject(PostProjectDto request) {
    var project = projectMapper.toEntity(request);
    var savedProject = projectRepository.save(project);
    return projectMapper.toDetailsDto(savedProject);
  }
}
