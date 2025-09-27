package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.webhook.WebhookProperties;

public class WebhookPropertiesModel {

  public static WebhookProperties basic() {
    final WebhookProperties properties = new WebhookProperties();
    properties.setAllowMissingSecret(true);
    properties.setWebhookSecret("top-secret");
    return properties;
  }

  public static WebhookProperties basic(final String secret) {
    final WebhookProperties properties = new WebhookProperties();
    properties.setAllowMissingSecret(false);
    properties.setWebhookSecret(secret);
    return properties;
  }

  public static WebhookProperties basic(final String secret, final boolean allowMissing) {
    final WebhookProperties properties = new WebhookProperties();
    properties.setAllowMissingSecret(allowMissing);
    properties.setWebhookSecret(secret);
    return properties;
  }
}
