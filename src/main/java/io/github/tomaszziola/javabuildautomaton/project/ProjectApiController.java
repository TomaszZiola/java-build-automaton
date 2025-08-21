package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.Build;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildMapper;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {

  private final BuildMapper buildMapper;
  private final ProjectMapper projectMapper;
  private final ProjectRepository projectRepository;
  private final BuildRepository buildRepository;

  public ProjectApiController(
      final BuildMapper buildMapper,
      final ProjectMapper mapper,
      final ProjectRepository projectRepository,
      final BuildRepository buildRepository) {
    this.buildMapper = buildMapper;
    this.projectMapper = mapper;
    this.projectRepository = projectRepository;
    this.buildRepository = buildRepository;
  }

  @GetMapping
  public List<ProjectDetailsDto> getAllProjects() {
    return projectRepository.findAll().stream().map(projectMapper::toDetailsDto).toList();
  }

  @GetMapping("/{projectId}/builds")
  public List<BuildSummaryDto> getProjectBuilds(@PathVariable final Long projectId) {
    final Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    final List<Build> builds = buildRepository.findByProject(project);
    return builds.stream().map(buildMapper::toSummaryDto).toList();
  }
}
