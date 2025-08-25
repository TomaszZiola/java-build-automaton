package io.github.tomaszziola.javabuildautomaton.webhook;

import static javax.crypto.Mac.getInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
  @DisplayName("Missing secret (null) allowed returns true")
  void missingSecretNullAllowedReturnsTrue() {
    // given
    webhookSecurityServiceImpl = new WebhookSecurityService(null, true);
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, body)).isTrue();
  }

  @Test
  @DisplayName("Valid signature returns true")
  void validSignatureReturnsTrue() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256Header, body)).isTrue();
  }

  @Test
  @DisplayName("Invalid signature returns false")
  void invalidSignatureReturnsFalse() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(invalidSha256Header, body)).isFalse();
  }

  @Test
  @DisplayName("Missing secret allowed returns true")
  void missingSecretAllowedReturnsTrue() {
    // when && then
    webhookSecurityServiceImpl = new WebhookSecurityService("", true);
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, body)).isTrue();
  }

  @Test
  @DisplayName("Missing secret not allowed returns false")
  void missingSecretNotAllowedReturnsFalse() {
    // given
    webhookSecurityServiceImpl = new WebhookSecurityService("", false);
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, body)).isFalse();
  }

  @Test
  @DisplayName("Invalid header format returns false when not starting with sha256=")
  void invalidHeaderFormatReturnsFalse() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(invalidHeader, body)).isFalse();
  }

  @Test
  @DisplayName("Null header returns false when secret configured")
  void nullHeaderReturnsFalseWhenSecretConfigured() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, body)).isFalse();
  }

  @Test
  @DisplayName("Invalid hex or length returns false")
  void invalidHexOrLengthReturnsFalse() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid("sha256=zz", body)).isFalse();
    assertThat(webhookSecurityServiceImpl.isSignatureValid("sha256=aa", body)).isFalse();
  }

  @Test
  @DisplayName("Null payload returns false when secret configured")
  void nullPayloadReturnsFalse() {
    // when && then
    assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256Header, null)).isFalse();
  }

  @Test
  @DisplayName("When Mac.getInstance throws NoSuchAlgorithmException then returns false")
  void returnsFalseWhenNoSuchAlgorithmExceptionCaught() {
    try (MockedStatic<Mac> macStatic = mockStatic(Mac.class)) {
      macStatic
          .when(() -> getInstance(anyString()))
          .thenThrow(new NoSuchAlgorithmException("forced by test"));

      assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256Header, body)).isFalse();
    }
  }

  @Test
  @DisplayName("When mac.init throws InvalidKeyException then returns false")
  void returnsFalseWhenInvalidKeyExceptionCaught() throws Exception {
    final Mac macMock = mock(Mac.class);
    try (MockedStatic<Mac> macStatic = mockStatic(Mac.class)) {
      macStatic.when(() -> getInstance(anyString())).thenReturn(macMock);
      doThrow(new InvalidKeyException("forced by test")).when(macMock).init(any());

      assertThat(webhookSecurityServiceImpl.isSignatureValid(validSha256Header, body)).isFalse();
    }
  }
}
