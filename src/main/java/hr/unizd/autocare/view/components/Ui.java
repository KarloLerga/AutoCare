package hr.unizd.autocare.view.components;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.WorkCategory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Standardni Swing layouti i formatiranje, bez poslovnih pravila. */
public final class Ui {
  public static final Color BACKGROUND = new Color(0xF4F7FB),
      INK = new Color(0x172B4D),
      ACCENT = new Color(0x176B87),
      MUTED = new Color(0x526477);
  public static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("dd.MM.uuuu.").withResolverStyle(ResolverStyle.STRICT);

  private Ui() {}

  public static JPanel column() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setOpaque(false);
    return p;
  }

  public static JPanel row(Component... controls) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
    p.setOpaque(false);
    for (Component c : controls) {
      p.add(c);
    }
    return p;
  }

  public static JPanel actions(Component... controls) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 4));
    p.setOpaque(false);
    for (Component c : controls) {
      p.add(c);
    }
    return p;
  }

  public static JPanel card() {
    JPanel p = new JPanel(new BorderLayout(12, 12));
    p.setBackground(Color.WHITE);
    p.setBorder(
        new CompoundBorder(new LineBorder(new Color(0xDCE5ED)), new EmptyBorder(16, 16, 16, 16)));
    return p;
  }

  public static JLabel heading(String title) {
    JLabel l = new JLabel(title);
    l.setFont(l.getFont().deriveFont(Font.BOLD, 24f));
    l.setForeground(INK);
    return l;
  }

  public static JLabel hint(String text) {
    JLabel l = new HintLabel(text);
    l.setForeground(MUTED);
    return l;
  }

  private static final class HintLabel extends JLabel {
    HintLabel(String value) {
      super(value);
    }

    @Override
    public void setText(String value) {
      String escaped =
          value == null
              ? ""
              : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      super.setText("<html>" + escaped + "</html>");
    }
  }

  public static JButton button(String title, boolean primary) {
    JButton b = new JButton(title);
    b.setFocusPainted(true);
    if (primary) {
      b.setBackground(ACCENT);
      b.setForeground(Color.WHITE);
    }
    return b;
  }

  public static JPanel form() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setOpaque(false);
    return p;
  }

  public static void field(JPanel form, int row, String label, JComponent component) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = row;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(6, 0, 6, 16);
    JLabel l = new JLabel(label);
    l.setLabelFor(component);
    form.add(l, c);
    c.gridx = 1;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    form.add(component, c);
  }

  public static JSpinner mileage(int value) {
    JSpinner s = new JSpinner(new SpinnerNumberModel(value, 0, 3_000_000, 100));
    s.setEditor(new JSpinner.NumberEditor(s, "0"));
    return s;
  }

  public static int integer(JSpinner s) {
    try {
      s.commitEdit();
      return ((Number) s.getValue()).intValue();
    } catch (java.text.ParseException ex) {
      throw new IllegalArgumentException("Unesite cijeli broj.");
    }
  }

  public static BigDecimal parseMoney(String text, boolean optional) {
    if (text == null || text.isBlank()) {
      if (optional) {
        return null;
      }
      throw new IllegalArgumentException("Unesite stvarno placenu cijenu za svaku stavku.");
    }
    String t = text.strip();
    if (!t.matches("[0-9]+([.,][0-9]{1,2})?")) {
      throw new IllegalArgumentException(
          "Cijena: npr. 120,50; bez simbola EUR i odvajanja tisucica.");
    }
    return Checks.money(new BigDecimal(t.replace(',', '.')), optional);
  }

  public static String money(BigDecimal value) {
    return value == null
        ? "Nepoznato"
        : String.format(Locale.forLanguageTag("hr-HR"), "%,.2f EUR", value);
  }

  public static String total(CostSummary c) {
    return money(c.getKnownTotal())
        + (c.getUnknownCount() > 0 ? " + " + c.getUnknownCount() + " nepoznatih" : "");
  }

  public static String date(LocalDate d) {
    return d == null ? "-" : d.format(DATE);
  }

  public static String km(Integer km) {
    return km == null ? "-" : String.format(Locale.forLanguageTag("hr-HR"), "%,d km", km);
  }

  public static String status(MaintenanceStatus s) {
    return switch (s) {
      case CONDITION_BASED -> "Prema stanju";
      case VEHICLE_INDICATOR -> "Prema indikatoru vozila";
      case OK -> "U redu";
      case SOON -> "Uskoro";
      case DUE -> "Dospjelo";
      case UNKNOWN_HISTORY -> "Nema povijesti";
      case UNKNOWN_INTERVAL -> "Interval nepoznat";
    };
  }

  public static String category(WorkCategory c) {
    return c == WorkCategory.MAINTENANCE ? "Odrzavanje" : "Popravak";
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
    JOptionPane.showMessageDialog(
        parent,
        error.getMessage() == null ? "Operacija nije uspjela." : error.getMessage(),
        "AutoCare",
        JOptionPane.ERROR_MESSAGE);
  }

  public static void escape(JDialog dialog, Runnable close) {
    dialog
        .getRootPane()
        .registerKeyboardAction(
            e -> close.run(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            close.run();
          }
        });
  }

  public static Map<Component, Boolean> disableTree(Component root) {
    Map<Component, Boolean> old = new IdentityHashMap<>();
    capture(root, old);
    return old;
  }

  private static void capture(Component c, Map<Component, Boolean> old) {
    old.put(c, c.isEnabled());
    c.setEnabled(false);
    if (c instanceof Container panel) {
      for (Component child : panel.getComponents()) {
        capture(child, old);
      }
    }
  }

  public static void restore(Map<Component, Boolean> old) {
    old.forEach(Component::setEnabled);
  }
}
