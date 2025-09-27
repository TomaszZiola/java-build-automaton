package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;

public record ProjectDetailsDto(
    Long id,
    String repositoryUrl,
    String name,
    String repositoryName,
    String slug,
    BuildTool buildTool) {}
