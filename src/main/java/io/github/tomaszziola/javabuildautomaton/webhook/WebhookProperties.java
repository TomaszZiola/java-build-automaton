package io.github.tomaszziola.javabuildautomaton.webhook;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {

  @Size(min = 5)
  private String webhookSecret;

  private boolean allowMissingSecret;
}
