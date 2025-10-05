package io.github.tomaszziola.javabuildautomaton.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookStartupVerifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookStartupVerifier.class);

  private final String secret;
  private final boolean allowMissing;

  public WebhookStartupVerifier(final WebhookProperties properties) {
    this.secret = properties.getWebhookSecret();
    this.allowMissing = properties.isAllowMissingSecret();
  }

  @Bean
  ApplicationRunner verifyWebhookSecretOnStartup() {
    return _ -> {
      if ((secret == null || secret.isBlank()) && !allowMissing) {
        final String msg =
            "Application misconfiguration: webhook.webhook-secret is empty but "
                + "webhook.allow-missing-webhook-secret=false. "
                + "Set WEBHOOK_WEBHOOK_SECRET or explicitly allow missing (ONLY for local debug).";
        LOGGER.error(msg);
        throw new MissingWebhookSecretException(msg);
      }
      if ((secret == null || secret.isBlank()) && allowMissing) {
        LOGGER.warn("Webhook secret is empty; validation is DISABLED (allow-missing=true).");
      } else {
        LOGGER.info("Webhook secret is configured; signature validation ENABLED.");
      }
    };
  }
}
