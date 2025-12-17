package io.github.tomaszziola.javabuildautomaton.webui;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

class WebUiControllerTest extends BaseUnit {

  @Test
  @DisplayName(
      "Given existing projects, when showing dashboard, then return dashboard view and projects in model")
  void returnsDashboardViewAndProjectsWhenShowDashboard() {
    // when
    var view = webUiControllerImpl.showDashboard(modelImpl);

    // then
    assertThat(view).isEqualTo("dashboard");
    assertThat(modelImpl.asMap()).containsEntry("projects", of(projectDto));
    verify(projectService).findAll();
  }

  @Test
  @DisplayName(
      "Given existing project, when showing project details, then return view and model attributes")
  void returnsProjectDetailsViewAndModelWhenShowProjectDetails() {
    // when
    var view = webUiControllerImpl.showProjectDetails(projectId, modelImpl);

    // then
    assertThat(view).isEqualTo("project-details");
    assertThat(modelImpl.asMap())
        .containsEntry("project", projectDto)
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
    var view = webUiControllerImpl.showBuildDetails(projectId, buildId, modelImpl);

    // then
    assertThat(view).isEqualTo("build-details");
    assertThat(modelImpl.asMap())
        .containsEntry("project", projectDto)
        .containsEntry("build", buildDetailsDto);
    verify(projectService).findDetailsById(projectId);
    verify(buildService).findBuildDetailsById(buildId);
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

  @Test
  @DisplayName(
      "Given request to show create project form, then return form view and include empty request and build tools")
  void returnsFormViewAndModelWhenShowCreateProjectForm() {
    // when
    var view = webUiControllerImpl.showCreateProjectForm(modelImpl);

    // then
    assertThat(view).isEqualTo("projects-create");
    assertThat(modelImpl.asMap())
        .containsEntry("request", new PostProjectDto())
        .containsEntry("buildTools", BuildTool.values());
  }

  @Test
  @DisplayName(
      "Given invalid request, when creating project, then return form view and include build tools")
  void returnsFormViewAndBuildToolsWhenCreateProjectHasErrors() {
    // given
    var errors = new BeanPropertyBindingResult(postProjectDto, "request");
    errors.reject("invalid");

    // when
    var view = webUiControllerImpl.createProject(postProjectDto, errors, modelImpl);

    // then
    assertThat(view).isEqualTo("projects-create");
    assertThat(modelImpl.asMap()).containsEntry("buildTools", BuildTool.values());
    verify(projectService, never()).saveProject(postProjectDto);
  }

  @Test
  @DisplayName("Given valid request, when creating project, then save and redirect to dashboard")
  void savesProjectAndRedirectsWhenCreateProjectIsValid() {
    // given
    var errors = new BeanPropertyBindingResult(postProjectDto, "request");

    // when
    var view = webUiControllerImpl.createProject(postProjectDto, errors, modelImpl);

    // then
    assertThat(view).isEqualTo("redirect:/");
    verify(projectService).saveProject(postProjectDto);
  }
}
