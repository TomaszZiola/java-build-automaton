package io.github.tomaszziola.javabuildautomaton.webui;

import io.github.tomaszziola.javabuildautomaton.api.dto.PostProjectDto;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildService;
import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion;
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

  private final BuildService buildService;
  private final ProjectService projectService;

  @GetMapping
  public String showDashboard(Model model) {
    var projects = projectService.findAll();

    model.addAttribute("projects", projects);

    return "dashboard";
  }

  @GetMapping("/projects/{projectId}")
  public String showProjectDetails(@PathVariable Long projectId, Model model) {
    var project = projectService.findDetailsById(projectId);
    var builds = projectService.findProjectBuilds(projectId);

    model.addAttribute("project", project);
    model.addAttribute("builds", builds);

    return "project-details";
  }

  @GetMapping("/projects/{projectId}/builds/{buildId}")
  public String showBuildDetails(
      @PathVariable Long projectId, @PathVariable Long buildId, Model model) {
    var project = projectService.findDetailsById(projectId);
    var build = buildService.findBuildDetailsById(buildId);

    model.addAttribute("project", project);
    model.addAttribute("build", build);

    return "build-details";
  }

  @GetMapping("/projects/create")
  public String showCreateProjectForm(Model model) {
    model.addAttribute("request", new PostProjectDto());
    model.addAttribute("buildTools", BuildTool.values());
    model.addAttribute("javaVersions", ProjectJavaVersion.values());
    return "projects-create";
  }

  @PostMapping("/projects/create")
  public String createProject(
      @ModelAttribute("request") @Valid PostProjectDto request,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("buildTools", BuildTool.values());
      return "projects-create";
    }

    projectService.saveProject(request);
    return "redirect:/";
  }
}
