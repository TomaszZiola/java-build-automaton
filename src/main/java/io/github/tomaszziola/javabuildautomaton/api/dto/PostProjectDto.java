package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import jakarta.validation.constraints.Pattern;

public record PostProjectDto(
    @Pattern(
            regexp = "^https://github\\.com/[^/]+/[^/]+\\.git$",
            message = "Repository URL must match pattern: https://github.com/{user}/{repo}.git")
        String repositoryUrl,
    BuildTool buildTool) {}
