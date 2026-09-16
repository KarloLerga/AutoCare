package hr.unizd.autocare.domain;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
/** Jednostavne zajednicke invarijante. IllegalArgumentException znaci nevaljan unos. */
public final class Checks {
    private Checks() {
    }
    public static String text(String value, int max, String label) {
        if (value==null || value.strip().isEmpty() || value.strip().length()>max) throw new IllegalArgumentException(label+": obavezan unos do "+max+" znakova.");
        return value.strip();
    }
    public static String optional(String value, int max, String label) {
        return value==null || value.isBlank() ? null : text(value, max, label);
    }
    public static String email(String value) {
        String email=text(value, 254, "E-mail").toLowerCase(Locale.ROOT);
        if (!email.matches("[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?\\.[a-z]{2,63}") || email.contains(".."))
        throw new IllegalArgumentException("Unesite valjanu e-mail adresu.");
        return email;
    }
    public static int mileage(int value) {
        if(value<0 || value>3_000_000) throw new IllegalArgumentException("Kilometraza mora biti 0 - 3.000.000 km.");
        return value;
    }
    public static BigDecimal money(BigDecimal value, boolean nullable) {
        if(value==null) {
            if(nullable)return null;
            throw new IllegalArgumentException("Unesite stvarno placenu cijenu.");
        }
        if(value.signum()<0 || value.compareTo(new BigDecimal("9999999.99"))>0) throw new IllegalArgumentException("Cijena mora biti od 0 do 9.999.999,99 EUR.");
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        }
        catch(ArithmeticException ex) {
            throw new IllegalArgumentException("Cijena smije imati najvise dvije decimale.");
        }
    }
    public static void password(char[] value) {
        if(value==null || value.length<12 || value.length>128) throw new IllegalArgumentException("Lozinka treba imati 12 - 128 znakova.");
    }
}
