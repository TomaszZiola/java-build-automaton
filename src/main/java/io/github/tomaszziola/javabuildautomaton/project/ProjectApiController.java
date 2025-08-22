package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {

  private final ProjectService projectService;

  public ProjectApiController(final ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  public List<ProjectDetailsDto> getAllProjects() {
    return projectService.findAll();
  }

  @GetMapping("/{projectId}/builds")
  public List<BuildSummaryDto> getProjectBuilds(@PathVariable final Long projectId) {
    return projectService.findProjectBuilds(projectId);
  }
}
