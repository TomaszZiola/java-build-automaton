package io.github.tomaszziola.javabuildautomaton.webhook;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.tomaszziola.javabuildautomaton.models.WebhookPropertiesModel;
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
    final Logger logger = (Logger) LoggerFactory.getLogger(WebhookStartupVerifier.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
    logger.setLevel(TRACE);
  }

  @Test
  @DisplayName("Given empty secret and allowMissing=false, when verifying, then throw exception")
  void throwsExceptionWhenSecretEmptyAndNotAllowMissing() {
    // given
    webhookProperties = WebhookPropertiesModel.basic("");
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
    webhookProperties = WebhookPropertiesModel.basic(null);
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

    // when & then
    assertThatThrownBy(() -> runner.run(null))
        .isInstanceOf(MissingWebhookSecretException.class)
        .hasMessageContaining("Application misconfiguration: app.github.webhook-secret is empty");
  }

  @Test
  @DisplayName("Given blank secret and allowMissing=false, when verifying, then throw exception")
  void throwsExceptionWhenSecretBlankAndNotAllowMissing() throws Exception {
    // given
    webhookProperties = WebhookPropertiesModel.basic("   ");
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
    webhookProperties = WebhookPropertiesModel.basic("", true);
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
    webhookProperties = WebhookPropertiesModel.basic(null, true);
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
    webhookProperties = WebhookPropertiesModel.basic("my secret key", true);
    webhookStartupVerifierImpl = new WebhookStartupVerifier(webhookProperties);
    final ApplicationRunner runner = webhookStartupVerifierImpl.verifyWebhookSecretOnStartup();

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
