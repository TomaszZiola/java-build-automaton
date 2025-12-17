package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload.PullRequest;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload.PullRequest.Base;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload.RepositoryInfo;

public final class WebhookPayloadModel {

  private WebhookPayloadModel() {}

  public static WebhookPayload basic() {
    return new WebhookPayload(
        "refs/heads/main",
        new RepositoryInfo("TomaszZiola/test"),
        new PullRequest(new Base("main")));
  }

  public static TestBuilder builder() {
    return new TestBuilder()
        .ref("refs/heads/main")
        .repository(new RepositoryInfo("TomaszZiola/test"))
        .pullRequest(new PullRequest(new Base("main")));
  }

  public static final class TestBuilder {
    private String ref;
    private RepositoryInfo repository;
    private PullRequest pullRequest;

    public TestBuilder ref(String ref) {
      this.ref = ref;
      return this;
    }

    public TestBuilder repository(RepositoryInfo repository) {
      this.repository = repository;
      return this;
    }

    public TestBuilder pullRequest(PullRequest pullRequest) {
      this.pullRequest = pullRequest;
      return this;
    }

    public WebhookPayload build() {
      return new WebhookPayload(ref, repository, pullRequest);
    }
  }
}
