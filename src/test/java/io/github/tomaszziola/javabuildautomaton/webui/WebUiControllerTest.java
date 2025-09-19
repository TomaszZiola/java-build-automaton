package io.github.tomaszziola.javabuildautomaton.webui;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebUiControllerTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given existing projects, when showing dashboard, then return dashboard view and projects in model")
  void returnsDashboardViewAndProjectsWhenShowDashboard() {
    // when
    final var view = webUiControllerImpl.showDashboard(modelImpl);

    // then
    assertThat(view).isEqualTo("dashboard");
    assertThat(modelImpl.asMap()).containsEntry("projects", of(projectDetailsDto));
    verify(projectService).findAll();
  }

  @Test
  @DisplayName(
      "Given existing project, when showing project details, then return view and model attributes")
  void returnsProjectDetailsViewAndModelWhenShowProjectDetails() {
    // when
    final var view = webUiControllerImpl.showProjectDetails(projectId, modelImpl);

    // then
    assertThat(view).isEqualTo("project-details");
    assertThat(modelImpl.asMap())
        .containsEntry("project", projectDetailsDto)
        .containsEntry("builds", of(buildSummaryDto));
    verify(projectService).findDetailsById(projectId);
    verify(projectService).findProjectBuilds(projectId);
  }

  @Test
  @DisplayName(
      "Given non-existing project, when showing project details, then throw ProjectNotFoundException")
  void throwsWhenProjectMissingOnShowProjectDetails() {
    // when / then
    assertThrows(
        ProjectNotFoundException.class,
        () -> webUiControllerImpl.showProjectDetails(nonExistentProjectId, modelImpl));
    verify(projectService, never()).findProjectBuilds(nonExistentProjectId);
  }

  @Test
  @DisplayName(
      "Given existing project, when showing build details, then return view and model attributes")
  void returnsBuildDetailsViewAndModelWhenShowBuildDetails() {
    // when
    final var view = webUiControllerImpl.showBuildDetails(projectId, buildId, modelImpl);

    // then
    assertThat(view).isEqualTo("build-details");
    assertThat(modelImpl.asMap())
        .containsEntry("project", projectDetailsDto)
        .containsEntry("build", buildDetailsDto);
    verify(projectService).findDetailsById(projectId);
    verify(projectService).findBuildDetailsById(buildId);
  }

  @Test
  @DisplayName(
      "Given non-existing build, when showing build details, then throw BuildNotFoundException")
  void throwsWhenBuildMissingOnShowBuildDetails() {
    // when / then
    assertThrows(
        BuildNotFoundException.class,
        () -> webUiControllerImpl.showBuildDetails(projectId, nonExistentBuildId, modelImpl));
    verify(projectService, never()).findProjectBuilds(nonExistentProjectId);
  }
}
