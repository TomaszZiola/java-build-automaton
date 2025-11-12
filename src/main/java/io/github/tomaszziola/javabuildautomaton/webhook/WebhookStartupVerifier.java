package io.github.tomaszziola.javabuildautomaton.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WebhookStartupVerifier {

  private final String secret;
  private final boolean allowMissing;

  public WebhookStartupVerifier(WebhookProperties properties) {
    this.secret = properties.getWebhookSecret();
    this.allowMissing = properties.isAllowMissingSecret();
  }

  @Bean
  ApplicationRunner verifyWebhookSecretOnStartup() {
    return _ -> {
      if ((secret == null || secret.isBlank()) && !allowMissing) {
        String msg =
            "Application misconfiguration: webhook.webhook-secret is empty but "
                + "webhook.allow-missing-webhook-secret=false. "
                + "Set WEBHOOK_WEBHOOK_SECRET or explicitly allow missing (ONLY for local debug).";
        log.error(msg);
        throw new MissingWebhookSecretException(msg);
      }
      if ((secret == null || secret.isBlank()) && allowMissing) {
        log.warn("Webhook secret is empty; validation is DISABLED (allow-missing=true).");
      } else {
        log.info("Webhook secret is configured; signature validation ENABLED.");
      }
    };
  }
}
