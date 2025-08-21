package io.github.tomaszziola.javabuildautomaton.api.dto;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus;
import java.time.Instant;

public record BuildSummaryDto(Long id, BuildStatus status, Instant startTime, Instant endTime) {}
