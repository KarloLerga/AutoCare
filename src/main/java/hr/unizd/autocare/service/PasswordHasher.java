package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** JDK PBKDF2, jedinstvena sol i usporedba bez ranog izlaska; nikad na EDT-u. */
public final class PasswordHasher {
  private static final int ITERATIONS = 600_000;
  private final SecureRandom random = new SecureRandom();

  public String hash(char[] password) {
    Checks.password(password);
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return "pbkdf2-sha256$"
        + ITERATIONS
        + "$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public boolean verify(char[] password, String encoded) {
    if (password == null || password.length > 128 || encoded == null) {
      return false;
    }
    try {
      String[] parts = encoded.split("\\$", -1);
      if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) {
        return false;
      }
      int iterations = Integer.parseInt(parts[1]);
      if (iterations < 100_000 || iterations > 2_000_000) {
        return false;
      }
      byte[] salt = Base64.getDecoder().decode(parts[2]),
          expected = Base64.getDecoder().decode(parts[3]);
      if (salt.length != 16 || expected.length != 32) {
        return false;
      }
      byte[] actual = derive(password, salt, iterations);
      boolean valid = MessageDigest.isEqual(expected, actual);
      Arrays.fill(actual, (byte) 0);
      return valid;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  private byte[] derive(char[] password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("PBKDF2 nije dostupan u JDK-u.", ex);
    } finally {
      spec.clearPassword();
    }
  }
}
