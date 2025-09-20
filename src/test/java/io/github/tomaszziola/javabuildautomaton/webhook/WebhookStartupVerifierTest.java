package io.github.tomaszziola.javabuildautomaton.webhook;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;

class WebhookStartupVerifierTest extends BaseUnit {

  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  void setUp() {
    Logger logger = (Logger) LoggerFactory.getLogger(WebhookStartupVerifier.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
    logger.setLevel(TRACE);
  }

  @Test
  @DisplayName("Given empty secret and allowMissing=false, when verifying, then throw exception")
  void throwsExceptionWhenSecretEmptyAndNotAllowMissing() {
    // given
    final String secret = "";
    final boolean allowMissing = false;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when & then
    assertThatThrownBy(() -> runner.run(null))
        .isInstanceOf(MissingWebhookSecretException.class)
        .hasMessageContaining("Application misconfiguration: app.github.webhook-secret is empty")
        .hasMessageContaining("app.github.allow-missing-webhook-secret=false")
        .hasMessageContaining("Set APP_GITHUB_WEBHOOK_SECRET or explicitly allow missing");
  }

  @Test
  @DisplayName("Given null secret and allowMissing=false, when verifying, then throw exception")
  void throwsExceptionWhenSecretNullAndNotAllowMissing() {
    // given
    final String secret = null;
    final boolean allowMissing = false;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when & then
    assertThatThrownBy(() -> runner.run(null))
        .isInstanceOf(MissingWebhookSecretException.class)
        .hasMessageContaining("Application misconfiguration: app.github.webhook-secret is empty");
  }

  @Test
  @DisplayName("Given blank secret and allowMissing=false, when verifying, then throw exception")
  void throwsExceptionWhenSecretBlankAndNotAllowMissing() throws Exception {
    // given
    final String secret = "   ";
    final boolean allowMissing = false;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when & then
    assertThatThrownBy(() -> runner.run(null))
        .isInstanceOf(MissingWebhookSecretException.class)
        .hasMessageContaining("Application misconfiguration: app.github.webhook-secret is empty");

    // verify error log
    assertThat(logAppender.list).extracting(ILoggingEvent::getLevel).contains(Level.ERROR);
  }

  @Test
  @DisplayName(
      "Given empty secret and allowMissing=true, when verifying, then log warning and succeed")
  void logsWarningWhenSecretEmptyAndAllowMissing() throws Exception {
    // given
    final String secret = "";
    final boolean allowMissing = true;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when
    runner.run(null);

    // then
    assertThat(logAppender.list).extracting(ILoggingEvent::getLevel).contains(Level.WARN);
    assertThat(logAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .anyMatch(
            msg ->
                msg.contains(
                    "Webhook secret is empty; validation is DISABLED (allow-missing=true)"));
  }

  @Test
  @DisplayName(
      "Given null secret and allowMissing=true, when verifying, then log warning and succeed")
  void logsWarningWhenSecretNullAndAllowMissing() throws Exception {
    // given
    final String secret = null;
    final boolean allowMissing = true;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when
    runner.run(null);

    // then
    assertThat(logAppender.list).extracting(ILoggingEvent::getLevel).contains(Level.WARN);
    assertThat(logAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .anyMatch(
            msg ->
                msg.contains(
                    "Webhook secret is empty; validation is DISABLED (allow-missing=true)"));
  }

  @Test
  @DisplayName("Given configured secret, when verifying, then log info and succeed")
  void logsInfoWhenSecretConfigured() throws Exception {
    // given
    final String secret = "my-secret-key";
    final boolean allowMissing = false;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when
    runner.run(null);

    // then
    assertThat(logAppender.list).extracting(ILoggingEvent::getLevel).contains(Level.INFO);
    assertThat(logAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .anyMatch(
            msg -> msg.contains("Webhook secret is configured; signature validation ENABLED"));
  }

  @Test
  @DisplayName(
      "Given configured secret with allowMissing=true, when verifying, then log info and succeed")
  void logsInfoWhenSecretConfiguredEvenWithAllowMissing() throws Exception {
    // given
    final String secret = "my-secret-key";
    final boolean allowMissing = true;
    final ApplicationRunner runner =
        webhookStartupVerifierImpl.verifyWebhookSecretOnStartup(secret, allowMissing);

    // when
    runner.run(null);

    // then
    assertThat(logAppender.list).extracting(ILoggingEvent::getLevel).contains(Level.INFO);
    assertThat(logAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .anyMatch(
            msg -> msg.contains("Webhook secret is configured; signature validation ENABLED"));
  }
}
