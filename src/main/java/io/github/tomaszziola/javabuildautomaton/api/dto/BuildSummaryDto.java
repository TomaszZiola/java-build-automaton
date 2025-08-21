package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus;
import java.time.LocalDateTime;

public record BuildSummaryDto(
    Long id, BuildStatus status, LocalDateTime startTime, LocalDateTime endTime) {}
