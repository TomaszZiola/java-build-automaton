package io.github.tomaszziola.javabuildautomaton.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(
    @JsonProperty("ref") String ref,
    @JsonProperty("repository") RepositoryInfo repository,
    @JsonProperty("pull_request") PullRequest pullRequest) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RepositoryInfo(@JsonProperty("full_name") String fullName) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PullRequest(@JsonProperty("base") Base base) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Base(@JsonProperty("ref") String ref) {}
  }
}
