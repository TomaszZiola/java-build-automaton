package io.github.tomaszziola.javabuildautomaton.webui;

import io.github.tomaszziola.javabuildautomaton.project.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebUiController {

  private final ProjectService projectService;

  public WebUiController(final ProjectService projectService) {
    this.projectService = projectService;
  }

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
}
