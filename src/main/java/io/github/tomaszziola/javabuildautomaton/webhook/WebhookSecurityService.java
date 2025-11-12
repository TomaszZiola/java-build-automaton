package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookSecurityService {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SIGNATURE_PREFIX = "sha256=";
  private static final int EXPECTED_SIGNATURE_BYTES = 32;

  private final Optional<SecretKeySpec> hmacKey;
  private final boolean allowMissingSecret;

  public WebhookSecurityService(WebhookProperties properties) {
    this.allowMissingSecret = properties.isAllowMissingSecret();
    var webhookSecret = properties.getWebhookSecret();
    this.hmacKey =
        (webhookSecret == null || webhookSecret.isBlank())
            ? empty()
            : of(new SecretKeySpec(webhookSecret.getBytes(UTF_8), HMAC_SHA256));
  }

  public boolean isSignatureValid(String signatureHeader, byte[] payloadBody) {
    if (hmacKey.isEmpty()) {
      if (allowMissingSecret) {
        log.warn(
            "Webhook secret is not configured. Skipping signature validation (allowMissingSecret=true).");
        return true;
      }
      log.error(
          "Webhook secret is not configured and allowMissingSecret=false. Rejecting request.");
      return false;
    }
    if (payloadBody == null) {
      log.warn("Received webhook with null payload body.");
      return false;
    }
    if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
      log.warn("Received webhook with missing or invalid signature header.");
      return false;
    }

    var hexPart = signatureHeader.substring(SIGNATURE_PREFIX.length()).toLowerCase(ROOT);
    byte[] providedSigBytes;
    try {
      providedSigBytes = HexFormat.of().parseHex(hexPart);
    } catch (IllegalArgumentException _) {
      log.warn("Invalid signature hex in header.");
      return false;
    }
    if (providedSigBytes.length != EXPECTED_SIGNATURE_BYTES) {
      log.warn(
          "Invalid signature length: expected {} bytes ({} hex chars), got {}",
          EXPECTED_SIGNATURE_BYTES,
          EXPECTED_SIGNATURE_BYTES * 2,
          providedSigBytes.length);
      return false;
    }

    try {
      var mac = Mac.getInstance(HMAC_SHA256);
      mac.init(hmacKey.get());
      var expectedSigBytes = mac.doFinal(payloadBody);

      return MessageDigest.isEqual(expectedSigBytes, providedSigBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("Error while verifying webhook signature", e);
      return false;
    }
  }
}
