package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;

public record ProjectDetailsDto(
    Long id, String name, String repositoryName, String localPath, BuildTool buildTool) {}
