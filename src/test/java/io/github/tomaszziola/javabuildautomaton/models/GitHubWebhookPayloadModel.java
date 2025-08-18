package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload.RepositoryInfo;

public final class GitHubWebhookPayloadModel {

  private GitHubWebhookPayloadModel() {}

  public static GitHubWebhookPayload basic() {
    return new GitHubWebhookPayload(new RepositoryInfo("TomaszZiola/test"));
  }
}
