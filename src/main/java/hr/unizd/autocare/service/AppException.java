package hr.unizd.autocare.service;

/** Ocekivana korisnicka pogreska poslovnog sloja. */
public class AppException extends RuntimeException {

  public AppException(String message) {
    super(message);
  }

  public AppException(String message, Throwable cause) {
    super(message, cause);
  }
}
