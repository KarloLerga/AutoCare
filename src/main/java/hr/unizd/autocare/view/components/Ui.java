package hr.unizd.autocare.view.components;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.WorkCategory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;

/** Standardni Swing layouti i formatiranje, bez poslovnih pravila. */
public final class Ui {
  public static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("dd.MM.uuuu.").withResolverStyle(ResolverStyle.STRICT);

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

  public static JButton button(String title) {
    return new JButton(title);
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

  public static BigDecimal parseMoney(String text, boolean optional) {
    if (text == null || text.isBlank()) {
      if (optional) {
        return null;
      }
      throw new IllegalArgumentException("Unesite stvarno plaćenu cijenu za svaku stavku.");
    }
    try {
      BigDecimal amount = new BigDecimal(text.strip().replace(',', '.'));
      return Checks.money(amount, optional);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Cijena mora biti broj, npr. 120,50.");
    }
  }

  public static String money(BigDecimal value) {
    if (value == null) {
      return "Nepoznato";
    }
    return String.format(Locale.forLanguageTag("hr-HR"), "%,.2f EUR", value);
  }

  public static BigDecimal roundedEstimate(BigDecimal amount) {
    if (amount == null) {
      return null;
    }
    return amount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
  }

  public static String estimate(BigDecimal amount) {
    BigDecimal rounded = roundedEstimate(amount);
    return rounded == null ? "Nema procjene" : "≈ " + rounded.toPlainString() + " EUR";
  }

  public static String total(CostSummary summary) {
    String result = money(summary.getKnownTotal());
    if (summary.getUnknownCount() > 0) {
      result += " + " + summary.getUnknownCount() + " nepoznatih";
    }
    return result;
  }

  public static String date(LocalDate date) {
    return date == null ? "-" : date.format(DATE);
  }

  public static String km(Integer mileage) {
    return mileage == null
        ? "-"
        : String.format(Locale.forLanguageTag("hr-HR"), "%,d km", mileage);
  }

  public static String status(MaintenanceStatus status) {
    if (status == MaintenanceStatus.OK) {
      return "U redu";
    }
    if (status == MaintenanceStatus.SOON) {
      return "Uskoro";
    }
    return "Dospjelo";
  }

  public static String problemStatus(hr.unizd.autocare.domain.ProblemStatus status) {
    return status == hr.unizd.autocare.domain.ProblemStatus.OPEN ? "Otvoren" : "Riješen";
  }

  public static String category(WorkCategory category) {
    return category == WorkCategory.MAINTENANCE ? "Održavanje" : "Popravak";
  }

  public static boolean confirm(Component parent, String text) {
    return JOptionPane.showConfirmDialog(
            parent, text, "AutoCare", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
        == JOptionPane.YES_OPTION;
  }

  public static void info(Component parent, String text) {
    JOptionPane.showMessageDialog(parent, text, "AutoCare", JOptionPane.INFORMATION_MESSAGE);
  }

  public static void error(Component parent, Throwable error) {
    String message = error.getMessage() == null ? "Operacija nije uspjela." : error.getMessage();
    JOptionPane.showMessageDialog(parent, message, "AutoCare", JOptionPane.ERROR_MESSAGE);
  }

  public static void escape(final JDialog dialog) {
    dialog
        .getRootPane()
        .registerKeyboardAction(
            new ActionListener() {
              @Override
              public void actionPerformed(java.awt.event.ActionEvent event) {
                dialog.dispose();
              }
            },
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }
}
