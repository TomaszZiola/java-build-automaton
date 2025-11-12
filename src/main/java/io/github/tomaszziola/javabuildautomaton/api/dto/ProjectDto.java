package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion;
import java.time.Instant;

public record ProjectDto(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String username,
    String repositoryName,
    String repositoryFullName,
    String repositoryUrl,
    BuildTool buildTool,
    ProjectJavaVersion javaVersion) {}
