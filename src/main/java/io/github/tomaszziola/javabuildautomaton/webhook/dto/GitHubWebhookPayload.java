package io.github.tomaszziola.javabuildautomaton.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWebhookPayload(
        @JsonProperty("repository")
        RepositoryInfo repositoryInfo
) {
    public record RepositoryInfo(
            @JsonProperty("full_name")
            String fullName
    ) {
    }
}
