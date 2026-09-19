package hr.unizd.autocare.service;

/** Očekivana korisnička pogreška poslovnog sloja. */
public class AppException extends RuntimeException {

  public AppException(String message) {
    super(message);
  }

  public AppException(String message, Throwable cause) {
    super(message, cause);
  }
}
