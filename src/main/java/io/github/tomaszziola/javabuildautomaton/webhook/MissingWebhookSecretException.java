package io.github.tomaszziola.javabuildautomaton.webhook;

import java.io.Serial;
import java.io.Serializable;

public class MissingWebhookSecretException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public MissingWebhookSecretException(final String message) {
    super(message);
  }
}
