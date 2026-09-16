package hr.unizd.autocare.service;
/** Ocekivana korisnicka pogreska ili kontrolirano prevedena infrastrukturna pogreska. */
public class AppException extends RuntimeException {
    public enum Kind {
        VALIDATION, AUTHENTICATION, NOT_FOUND, CONFLICT, DATABASE, COMMIT_UNKNOWN
    }
    private final Kind kind;
    public AppException(Kind kind, String message) {
        super(message);
        this.kind=kind;
    }
    public AppException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind=kind;
    }
    public Kind getKind() {
        return kind;
    }
    public static AppException validation(String text) {
        return new AppException(Kind.VALIDATION, text);
    }
    public static AppException conflict(String text) {
        return new AppException(Kind.CONFLICT, text);
    }
}
