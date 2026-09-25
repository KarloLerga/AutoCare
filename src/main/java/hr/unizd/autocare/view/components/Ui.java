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

/** Standardni Swing layouti i formatiranje, bez poslovnih pravila. */
public final class Ui {
  public static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("dd.MM.yyyy.");

  private Ui() {}

  public static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    return panel;
  }

  public static JPanel row(Component... controls) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
    panel.setOpaque(false);
    for (Component control : controls) {
      panel.add(control);
    }
    return panel;
  }

  public static JPanel actions(Component... controls) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 4));
    panel.setOpaque(false);
    for (Component control : controls) {
      panel.add(control);
    }
    return panel;
  }

  public static JPanel card() {
    JPanel panel = new JPanel(new BorderLayout(12, 12));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new java.awt.Color(95, 105, 120)),
        BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    return panel;
  }

  public static JLabel heading(String title) {
    JLabel label = new JLabel(title);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
    return label;
  }

  public static JLabel hint(String text) {
    JLabel label = new JLabel(text);
    if (javax.swing.UIManager.getColor("Label.disabledForeground") != null) {
      label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
    }
    return label;
  }

  public static JPanel form() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    return panel;
  }

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

  public static int mileage(JTextField field) {
    return Checks.mileage(integer(field));
  }

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

  public static String money(BigDecimal value) {
    return String.format(Locale.forLanguageTag("hr-HR"), "%,.2f EUR", value);
  }

  public static String priceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    if (minPrice == null || maxPrice == null) {
      return "Nema procjene";
    }
    return plainMoney(minPrice) + " - " + plainMoney(maxPrice) + " EUR";
  }

  private static String plainMoney(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  public static String workCategory(WorkCategory category) {
    if (category == WorkCategory.MAINTENANCE) {
      return "Održavanje";
    }
    return "Popravak";
  }

  public static String date(LocalDate date) {
    if (date == null) {
      return "-";
    }
    return date.format(DATE);
  }

  public static String km(Integer mileage) {
    if (mileage == null) {
      return "-";
    }
    return String.format(Locale.forLanguageTag("hr-HR"), "%,d km", mileage);
  }

  public static String problemStatus(boolean resolved) {
    if (resolved) {
      return "Riješen";
    }
    return "Otvoren";
  }


  public static void info(Component parent, String text) {
    JOptionPane.showMessageDialog(parent, text, "AutoCare", JOptionPane.INFORMATION_MESSAGE);
  }

  public static void error(Component parent, Throwable error) {
    String message = error.getMessage();
    if (message == null || message.isBlank()) {
      message = "Operacija nije uspjela.";
    }
    JOptionPane.showMessageDialog(parent, message, "AutoCare", JOptionPane.ERROR_MESSAGE);
  }

}
