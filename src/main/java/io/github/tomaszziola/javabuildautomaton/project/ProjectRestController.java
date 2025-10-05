package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.BuildSummaryDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectRestController {

  private final ProjectService service;

  @GetMapping
  public List<ProjectDto> getAllProjects() {
    return service.findAll();
  }

  @GetMapping("/{projectId}/builds")
  public List<BuildSummaryDto> getProjectBuilds(@PathVariable final Long projectId) {
    return service.findProjectBuilds(projectId);
  }

  @PostMapping("/create")
  public ProjectDto createProject(@RequestBody @Valid final PostProjectDto request) {
    return service.saveProject(request);
  }
}
