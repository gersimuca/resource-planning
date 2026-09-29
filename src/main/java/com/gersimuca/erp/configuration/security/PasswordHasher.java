package com.gersimuca.erp.configuration.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/**
 * Password hashing using PBKDF2-HMAC-SHA256, entirely via the JDK's built-in {@code javax.crypto}
 * -- no bcrypt/scrypt/Argon2 library. Stored format: {@code "iterations:saltBase64:hashBase64"}.
 */
@Component
public class PasswordHasher {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int SALT_BYTES = 16;
  private static final int KEY_LENGTH_BITS = 256;
  // OWASP-ballpark iteration count for PBKDF2-HMAC-SHA256 as of writing. Benchmark on your own
  // hardware (aim for ~500ms-1s per hash) and raise this over time as hardware gets faster.
  private static final int ITERATIONS = 600_000;

  private final SecureRandom secureRandom = new SecureRandom();

  public String hash(final String rawPassword) {
    final byte[] salt = new byte[SALT_BYTES];
    secureRandom.nextBytes(salt);
    final char[] chars = rawPassword.toCharArray();
    try {
      final byte[] hash = pbkdf2(chars, salt, ITERATIONS);
      return ITERATIONS + ":" + b64(salt) + ":" + b64(hash);
    } finally {
      Arrays.fill(chars, '\0');
    }
  }

  public boolean verify(final String rawPassword, final String stored) {
    if (rawPassword == null || stored == null) {
      return false;
    }
    final char[] chars = rawPassword.toCharArray();
    try {
      final String[] parts = stored.split(":");
      if (parts.length != 3) {
        return false;
      }
      final int iterations = Integer.parseInt(parts[0]);
      final byte[] salt = Base64.getDecoder().decode(parts[1]);
      final byte[] expected = Base64.getDecoder().decode(parts[2]);
      final byte[] actual = pbkdf2(chars, salt, iterations);
      return MessageDigest.isEqual(expected, actual);
    } catch (final Exception e) {
      // Malformed stored hash, bad base64, null passwordHash, etc. -- "does not match", not a
      // thrown error. In particular this is how a not-yet-migrated user (passwordHash == null)
      // is rejected.
      return false;
    } finally {
      Arrays.fill(chars, '\0');
    }
  }

  private byte[] pbkdf2(final char[] password, final byte[] salt, final int iterations) {
    try {
      final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
      final SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
      return factory.generateSecret(spec).getEncoded();
    } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Password hashing failed", e);
    }
  }

  private String b64(final byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }
}
