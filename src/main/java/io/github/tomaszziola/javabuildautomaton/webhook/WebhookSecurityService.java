package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookSecurityService {

  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String SIGNATURE_PREFIX = "sha256=";

  public boolean isSignatureValid(String signatureHeader, byte[] payloadBody,
      String webhookSecret) {

    if (payloadBody == null) {
      log.warn("Payload body is null");
      return false;
    }
    if (webhookSecret == null || webhookSecret.isBlank()) {
      log.warn("Webhook secret is null or blank");
      return false;
    }
    if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
      log.warn("Missing or invalid signature header format");
      return false;
    }

    var hexSignature = signatureHeader.substring(SIGNATURE_PREFIX.length());
    var providedSignature = hexToBytes(hexSignature);

    if (providedSignature == null || providedSignature.length != 32) {
      log.warn("Invalid signature length");
      return false;
    }

    try {
      var key = new SecretKeySpec(
          webhookSecret.getBytes(UTF_8),
          HMAC_ALGORITHM
      );
      var mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(key);
      var computedSignature = mac.doFinal(payloadBody);

      return MessageDigest.isEqual(computedSignature, providedSignature);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("HMAC computation failed", e);
      return false;
    }
  }

  private byte[] hexToBytes(String hex) {
    try {
      return HexFormat.of().parseHex(hex);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid hex string in signature: {}", e.getMessage());
      return new byte[0];
    }
  }
}
