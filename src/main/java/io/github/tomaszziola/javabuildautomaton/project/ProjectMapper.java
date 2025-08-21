package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.api.dto.ProjectDetailsDto;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

  public ProjectDetailsDto toDetailsDto(final Project project) {
    return new ProjectDetailsDto(
        project.getId(),
        project.getName(),
        project.getRepositoryName(),
        project.getLocalPath(),
        project.getBuildTool());
  }
}
