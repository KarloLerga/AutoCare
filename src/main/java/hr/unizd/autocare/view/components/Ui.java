package hr.unizd.autocare.view.components;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.WorkCategory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

/**
 * Zajednički Swing helper za ponavljajuće layout obrasce, formatiranje, parsiranje i poruke.
 *
 * <p>Klasa nema poslovno stanje. Cilj joj je ukloniti dupliciranje jednostavnog UI koda između
 * Viewova i zadržati jednako formatiranje datuma, kilometraže, cijena i poruka.
 */
public class Ui {
  /** Format datuma koji se prikazuje i prihvaća u korisničkom sučelju. */
  public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

  /** Sprječava stvaranje instance utility klase. */
  private Ui() {}

  /** Stvara proziran panel koji raspoređuje dodane komponente jednu ispod druge.
   *
   * @return panel konfiguriran za vertikalno slaganje komponenti
   */
  public static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    return panel;
  }

  /**
   * Stvara vodoravni red i u njega dodaje zadane komponente s ujednačenim razmacima.
   *
   * @param controls komponente reda
   * @return pripremljeni panel reda
   */
  public static JPanel row(Component... controls) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
    panel.setOpaque(false);
    for (Component control : controls) {
      panel.add(control);
    }
    return panel;
  }

  /**
   * Stvara red namijenjen grupiranju akcijskih gumba pri dnu forme ili dijaloga.
   *
   * @param controls akcijske komponente
   * @return panel akcija
   */
  public static JPanel actions(Component... controls) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 4));
    panel.setOpaque(false);
    for (Component control : controls) {
      panel.add(control);
    }
    return panel;
  }

  /** Stvara sadržajnu karticu s obrubom, unutarnjim razmakom i BorderLayoutom.
   *
   * @return panel stiliziran kao sadržajna kartica aplikacije
   */
  public static JPanel card() {
    JPanel panel = new JPanel(new BorderLayout(12, 12));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new java.awt.Color(95, 105, 120)),
        BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    return panel;
  }

  /**
   * Stvara labelu formatiranu kao glavni naslov sekcije.
   *
   * @param title tekst naslova
   * @return formatirana naslovna labela
   */
  public static JLabel heading(String title) {
    JLabel label = new JLabel(title);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
    return label;
  }

  /**
   * Stvara labelu stiliziranu kao sekundarnu napomenu.
   *
   * @param text pomoćni tekst
   * @return formatirana labela napomene
   */
  public static JLabel hint(String text) {
    JLabel label = new JLabel(text);
    if (javax.swing.UIManager.getColor("Label.disabledForeground") != null) {
      label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
    }
    return label;
  }

  /** Stvara proziran panel za redove forme koje metoda {@link #field} popunjava.
   *
   * @return panel s GridBagLayoutom pripremljen za labela-polje raspored forme
   */
  public static JPanel form() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    return panel;
  }

  /**
   * Dodaje jedan naslov i pripadajuću komponentu u zadani red forme.
   *
   * @param form panel forme
   * @param row indeks retka
   * @param title naziv polja
   * @param component Swing komponenta za unos ili prikaz
   */
  public static void field(JPanel form, int row, String title, JComponent component) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.anchor = GridBagConstraints.LINE_START;
    constraints.insets = new Insets(6, 0, 6, 16);
    JLabel label = new JLabel(title);
    label.setLabelFor(component);
    form.add(label, constraints);
    constraints.gridx = 1;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    form.add(component, constraints);
  }

  /**
   * Parsira cijeli broj iz tekstualnog polja.
   *
   * @param field polje s brojčanom vrijednošću
   * @return parsirani cijeli broj
   * @throws IllegalArgumentException ako vrijednost nije valjan cijeli broj
   */
  public static int integer(JTextField field) {
    if (field == null || field.getText().isBlank()) {
      throw new IllegalArgumentException("Unesite cijeli broj.");
    }
    try {
      return Integer.parseInt(field.getText().strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Unesite cijeli broj.");
    }
  }

  /**
   * Parsira kilometražu iz tekstualnog polja i provjerava je kroz domensko pravilo Checks.
   *
   * @param field polje kilometraže
   * @return valjana kilometraža
   * @throws IllegalArgumentException ako unos nije cijeli broj ili je kilometraža nedopuštena
   */
  public static int mileage(JTextField field) {
    return Checks.mileage(integer(field));
  }

  /**
   * Parsira korisnički datum u formatu koji aplikacija prikazuje.
   *
   * @param text tekst datuma
   * @return parsirani LocalDate
   * @throws IllegalArgumentException ako tekst nije valjan datum očekivanog formata
   */
  public static LocalDate parseDate(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("Unesite datum, npr. 15.09.2026.");
    }
    try {
      return LocalDate.parse(text.strip(), DATE);
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("Datum mora biti valjan, npr. 15.09.2026.");
    }
  }

  /**
   * Parsira decimalni novčani iznos iz korisničkog unosa i provjerava da iznos nije negativan.
   *
   * @param text tekstualni iznos
   * @return valjani BigDecimal iznos
   * @throws IllegalArgumentException ako iznos nije valjan broj ili je negativan
   */
  public static BigDecimal parseMoney(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("Unesite stvarno plaćenu cijenu za svaku stavku.");
    }
    try {
      BigDecimal amount = new BigDecimal(text.strip().replace(',', '.'));
      return Checks.money(amount);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Cijena mora biti broj, npr. 120,50.");
    }
  }

  /**
   * Formatira stvarni novčani iznos za prikaz u eurima.
   *
   * @param value iznos
   * @return korisnički formatirana vrijednost
   */
  public static String money(BigDecimal value) {
    return String.format(Locale.forLanguageTag("hr-HR"), "%,.2f EUR", value);
  }

  /**
   * Formatira informativni raspon cijena iz kataloga.
   *
   * @param minPrice donja granica procjene
   * @param maxPrice gornja granica procjene
   * @return tekst raspona prikladan za katalog
   */
  public static String priceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    if (minPrice == null || maxPrice == null) {
      return "Nema procjene";
    }
    return plainMoney(minPrice) + " - " + plainMoney(maxPrice) + " EUR";
  }

  /**
   * Formatira decimalnu vrijednost bez valute za ponovnu upotrebu u drugim UI formatima.
   *
   * @param value iznos
   * @return formatirani broj
   */
  private static String plainMoney(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  /**
   * Pretvara internu kategoriju rada u naziv prikladan za korisničko sučelje.
   *
   * @param category vrsta rada
   * @return lokalizirani naziv vrste rada
   */
  public static String workCategory(WorkCategory category) {
    if (category == WorkCategory.MAINTENANCE) {
      return "Održavanje";
    }
    return "Popravak";
  }

  /**
   * Formatira LocalDate u dosljedni hrvatski prikaz datuma.
   *
   * @param date datum ili {@code null}
   * @return formatirani datum ili oznaka za nedostupnu vrijednost
   */
  public static String date(LocalDate date) {
    if (date == null) {
      return "-";
    }
    return date.format(DATE);
  }

  /**
   * Formatira kilometražu s jedinicom mjere.
   *
   * @param mileage kilometraža ili {@code null}
   * @return formatirana kilometraža ili oznaka za nedostupnu vrijednost
   */
  public static String km(Integer mileage) {
    if (mileage == null) {
      return "-";
    }
    return String.format(Locale.forLanguageTag("hr-HR"), "%,d km", mileage);
  }

  /**
   * Pretvara logičko stanje problema u korisnički tekst.
   *
   * @param resolved je li problem povezan sa servisom rješenja
   * @return tekst otvorenog ili riješenog statusa
   */
  public static String problemStatus(boolean resolved) {
    if (resolved) {
      return "Riješen";
    }
    return "Otvoren";
  }

  /**
   * Prikazuje standardnu informativnu poruku vezanu uz zadanu Swing komponentu.
   *
   * @param parent roditeljska komponenta dijaloga
   * @param text tekst poruke
   */
  public static void info(Component parent, String text) {
    JOptionPane.showMessageDialog(parent, text, "AutoCare", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Pretvara iznimku u standardnu korisničku poruku o pogrešci i prikazuje je bez rušenja GUI-a.
   *
   * @param parent roditeljska komponenta dijaloga
   * @param error iznimka nastala tijekom korisničke akcije
   */
  public static void error(Component parent, Throwable error) {
    String message = error.getMessage();
    if (message == null || message.isBlank()) {
      message = "Operacija nije uspjela.";
    }
    JOptionPane.showMessageDialog(parent, message, "AutoCare", JOptionPane.ERROR_MESSAGE);
  }
}
