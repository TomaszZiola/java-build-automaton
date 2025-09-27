package io.github.tomaszziola.javabuildautomaton.webhook;

import static javax.crypto.Mac.getInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.github.tomaszziola.javabuildautomaton.models.WebhookPropertiesModel;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@SuppressWarnings("PMD.TooManyMethods")
class WebhookSecurityServiceTest extends BaseUnit {

  @Test
  @DisplayName("Given missing secret allowed, when validating signature, then return true")
  void returnsTrueWhenMissingSecretAllowed() {
    // given
    webhookProperties = WebhookPropertiesModel.basic(null, true);
    webhookSecurityServiceImpl = new WebhookSecurityService(webhookProperties);
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, bodyBytes)).isTrue();
  }

  @Test
  @DisplayName("Given isValid signature, when validating signature, then return true")
  void returnsTrueForValidSignature() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256HeaderValue, bodyBytes))
        .isTrue();
  }

  @Test
  @DisplayName("Given invalid signature, when validating signature, then return false")
  void returnsFalseForInvalidSignature() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(invalidSha256HeaderValue, bodyBytes))
        .isFalse();
  }

  @Test
  @DisplayName("Given empty secret allowed, when validating signature, then return true")
  void returnsTrueWhenEmptySecretAllowed() {
    // when && then
    webhookProperties = WebhookPropertiesModel.basic("", true);
    webhookSecurityServiceImpl = new WebhookSecurityService(webhookProperties);
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, bodyBytes)).isTrue();
  }

  @Test
  @DisplayName("Given missing secret not allowed, when validating signature, then return false")
  void returnsFalseWhenMissingSecretNotAllowed() {
    // given
    webhookProperties = WebhookPropertiesModel.basic("");
    webhookSecurityServiceImpl = new WebhookSecurityService(webhookProperties);
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, bodyBytes)).isFalse();
  }

  @Test
  @DisplayName(
      "Given header not starting with sha256=, when validating signature, then return false")
  void returnsFalseWhenHeaderNotStartingWithSha256Prefix() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(invalidSha256HeaderValue, bodyBytes))
        .isFalse();
  }

  @Test
  @DisplayName(
      "Given configured secret and null header, when validating signature, then return false")
  void returnsFalseWhenHeaderNullAndSecretConfigured() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, bodyBytes)).isFalse();
  }

  @Test
  @DisplayName("Given invalid hex or length, when validating signature, then return false")
  void returnsFalseForInvalidHexOrLength() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid("sha256=zz", bodyBytes)).isFalse();
    assertThat(webhookSecurityServiceImpl.isSignatureValid("sha256=aa", bodyBytes)).isFalse();
  }

  @Test
  @DisplayName(
      "Given configured secret and null payload, when validating signature, then return false")
  void returnsFalseWhenPayloadNullAndSecretConfigured() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256HeaderValue, null)).isFalse();
  }

  @Test
  @DisplayName("Given crypto algorithm unavailable, when validating signature, then return false")
  void returnsFalseWhenAlgorithmUnavailable() {
    try (MockedStatic<Mac> macStatic = mockStatic(Mac.class)) {
      macStatic
          .when(() -> getInstance(anyString()))
          .thenThrow(new NoSuchAlgorithmException("forced by test"));

      // when && then
      assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256HeaderValue, bodyBytes))
          .isFalse();
    }
  }

  @Test
  @DisplayName("Given invalid key during mac init, when validating signature, then return false")
  void returnsFalseWhenInvalidKeyDuringInit() throws Exception {
    // given
    final Mac macMock = mock(Mac.class);
    try (MockedStatic<Mac> macStatic = mockStatic(Mac.class)) {
      macStatic.when(() -> getInstance(anyString())).thenReturn(macMock);
      doThrow(new InvalidKeyException("forced by test")).when(macMock).init(any());

      // when && then
      assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256HeaderValue, bodyBytes))
          .isFalse();
    }
  }
}
