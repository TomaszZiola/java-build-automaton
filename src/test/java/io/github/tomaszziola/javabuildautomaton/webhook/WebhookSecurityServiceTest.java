package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.HexFormat.of;
import static javax.crypto.Mac.getInstance;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookSecurityServiceTest extends BaseUnit {

  private static String hmacSha256Hex(final String secret, final byte[] body) {
    try {
      final Mac mac = getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256"));
      final byte[] sig = mac.doFinal(body);
      return of().formatHex(sig);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("Failed to compute HMAC-SHA256 for test data", e);
    }
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
    final var webhookSecurityServiceImpl = new WebhookSecurityService("", true);
    assertThat(webhookSecurityServiceImpl.isSignatureValid(null, body)).isTrue();
  }

  @Test
  @DisplayName("Missing secret not allowed returns false")
  void missingSecretNotAllowedReturnsFalse() {
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
}
