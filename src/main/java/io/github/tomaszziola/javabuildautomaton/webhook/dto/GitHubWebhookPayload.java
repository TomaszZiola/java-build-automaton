package io.github.tomaszziola.javabuildautomaton.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWebhookPayload(
    @JsonProperty("ref") String ref, @JsonProperty("repository") RepositoryInfo repository) {
  public record RepositoryInfo(@JsonProperty("full_name") String fullName) {}
}
