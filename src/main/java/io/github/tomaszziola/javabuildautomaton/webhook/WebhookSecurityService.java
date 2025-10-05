package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class WebhookSecurityService {

  private static final Logger LOGGER = getLogger(WebhookSecurityService.class);
  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SIGNATURE_PREFIX = "sha256=";
  private static final int EXPECTED_SIGNATURE_BYTES = 32;

  private final Optional<SecretKeySpec> hmacKey;
  private final boolean allowMissingSecret;

  public WebhookSecurityService(final WebhookProperties properties) {
    this.allowMissingSecret = properties.isAllowMissingSecret();
    final var webhookSecret = properties.getWebhookSecret();
    this.hmacKey =
        (webhookSecret == null || webhookSecret.isBlank())
            ? empty()
            : of(new SecretKeySpec(webhookSecret.getBytes(UTF_8), HMAC_SHA256));
  }

  public boolean isSignatureValid(final String signatureHeader, final byte[] payloadBody) {
    if (hmacKey.isEmpty()) {
      if (allowMissingSecret) {
        LOGGER.warn(
            "Webhook secret is not configured. Skipping signature validation (allowMissingSecret=true).");
        return true;
      }
      LOGGER.error(
          "Webhook secret is not configured and allowMissingSecret=false. Rejecting request.");
      return false;
    }
    if (payloadBody == null) {
      LOGGER.warn("Received webhook with null payload body.");
      return false;
    }
    if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
      LOGGER.warn("Received webhook with missing or invalid signature header.");
      return false;
    }

    final var hexPart = signatureHeader.substring(SIGNATURE_PREFIX.length()).toLowerCase(ROOT);
    final byte[] providedSigBytes;
    try {
      providedSigBytes = HexFormat.of().parseHex(hexPart);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("Invalid signature hex in header.");
      return false;
    }
    if (providedSigBytes.length != EXPECTED_SIGNATURE_BYTES) {
      LOGGER.warn(
          "Invalid signature length: expected {} bytes ({} hex chars), got {}",
          EXPECTED_SIGNATURE_BYTES,
          EXPECTED_SIGNATURE_BYTES * 2,
          providedSigBytes.length);
      return false;
    }

    try {
      final var mac = Mac.getInstance(HMAC_SHA256);
      mac.init(hmacKey.get());
      final var expectedSigBytes = mac.doFinal(payloadBody);

      return MessageDigest.isEqual(expectedSigBytes, providedSigBytes);
    } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
      LOGGER.error("Error while verifying webhook signature", e);
      return false;
    }
  }
}
