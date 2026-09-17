package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hashiranje korisnicke lozinke pomocu PBKDF2 iz JDK-a. */
public final class PasswordHasher {
  private static final int ITERATIONS = 600_000;
  private final SecureRandom random = new SecureRandom();

  public String hash(char[] password) {
    Checks.password(password);

    byte[] salt = new byte[16];
    random.nextBytes(salt);

    return Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(password, salt));
  }

  public boolean verify(char[] password, String encoded) {
    if (password == null || encoded == null) {
      return false;
    }

    try {
      String[] parts = encoded.split(":", -1);
      if (parts.length != 2) {
        return false;
      }

      byte[] salt = Base64.getDecoder().decode(parts[0]);
      byte[] expected = Base64.getDecoder().decode(parts[1]);
      byte[] actual = derive(password, salt);

      return MessageDigest.isEqual(expected, actual);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private byte[] derive(char[] password, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, 256);

    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
          .generateSecret(spec)
          .getEncoded();
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("PBKDF2 nije dostupan u JDK-u.", exception);
    } finally {
      spec.clearPassword();
    }
  }
}
