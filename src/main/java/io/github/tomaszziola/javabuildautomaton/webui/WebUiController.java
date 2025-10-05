package io.github.tomaszziola.javabuildautomaton.webui;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.values;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebUiController {

  private final ProjectService projectService;

  @GetMapping
  public String showDashboard(final Model model) {
    final var projects = projectService.findAll();

    model.addAttribute("projects", projects);

    return "dashboard";
  }

  @GetMapping("/projects/{projectId}")
  public String showProjectDetails(@PathVariable final Long projectId, final Model model) {
    final var project = projectService.findDetailsById(projectId);
    final var builds = projectService.findProjectBuilds(projectId);

    model.addAttribute("project", project);
    model.addAttribute("builds", builds);

    return "project-details";
  }

  @GetMapping("/projects/{projectId}/builds/{buildId}")
  public String showBuildDetails(
      @PathVariable final Long projectId, @PathVariable final Long buildId, final Model model) {
    final var project = projectService.findDetailsById(projectId);
    final var build = projectService.findBuildDetailsById(buildId);

    model.addAttribute("project", project);
    model.addAttribute("build", build);

    return "build-details";
  }

  @GetMapping("/projects/create")
  public String showCreateProjectForm(final Model model) {
    model.addAttribute("request", new PostProjectDto(null, null));
    model.addAttribute("buildTools", values());
    return "projects-create";
  }

  @PostMapping("/projects/create")
  public String createProject(
      @ModelAttribute("request") @Valid final PostProjectDto request,
      final BindingResult bindingResult,
      final Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("buildTools", values());
      return "projects-create";
    }

    projectService.saveProject(request);
    return "redirect:/";
  }
}
