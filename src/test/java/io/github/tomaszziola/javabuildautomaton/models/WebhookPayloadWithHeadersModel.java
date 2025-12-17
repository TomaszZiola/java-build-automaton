package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayload;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayloadWithHeaders;

public class WebhookPayloadWithHeadersModel {

  public WebhookPayloadWithHeadersModel() {}

  public static WebhookPayloadWithHeaders basic() {
    return new WebhookPayloadWithHeaders(
        WebhookPayloadModel.basic(), "a65977c0-aa67-11f0-9e18-5a74c246d36d", "pull_request");
  }

  public static TestBuilder builder() {
    return new TestBuilder()
        .payload(WebhookPayloadModel.basic())
        .deliveryId("a65977c0-aa67-11f0-9e18-5a74c246d36d")
        .event("push");
  }

  public static class TestBuilder {
    private WebhookPayload payload;
    private String deliveryId;
    private String event;

    public TestBuilder payload(WebhookPayload payload) {
      this.payload = payload;
      return this;
    }

    public TestBuilder deliveryId(String deliveryId) {
      this.deliveryId = deliveryId;
      return this;
    }

    public TestBuilder event(String event) {
      this.event = event;
      return this;
    }

    public WebhookPayloadWithHeaders build() {
      return new WebhookPayloadWithHeaders(payload, deliveryId, event);
    }
  }
}
