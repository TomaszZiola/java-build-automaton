package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import java.time.Instant;

public record ProjectDetailsDto(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    String repositoryName,
    String repositoryUrl,
    String slug,
    BuildTool buildTool) {}
