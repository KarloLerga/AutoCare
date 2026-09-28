package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/** Prikazuje četiri sažeta pokazatelja aktivnog vozila: kilometražu, trošak, probleme i održavanje. */
public class DashboardView extends JPanel {
  /** Prikaz ukupnog stvarnog troška evidentiranih servisa. */
  private final JLabel total = Ui.hint("-");

  /** Prikaz najbližeg ili već dospjelog održavanja. */
  private final JLabel maintenance = Ui.hint("-");

  /** Broj otvorenih problema aktivnog vozila. */
  private final JLabel problems = Ui.hint("-");

  /** Trenutačna kilometraža aktivnog vozila. */
  private final JLabel mileage = Ui.hint("-");

  /** Stvara raspored Dashboard kartica i njihove početne vrijednosti. */
  public DashboardView() {
    super(new BorderLayout(16, 16));
    setOpaque(false);
    add(Ui.heading("Pregled vozila"), BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
    grid.setOpaque(false);
    addCard(grid, "Ukupni stvarni troškovi", total, FontAwesomeSolid.EURO_SIGN);
    addCard(grid, "Sljedeće održavanje", maintenance, FontAwesomeSolid.WRENCH);
    addCard(grid, "Otvoreni problemi", problems, FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    addCard(grid, "Trenutna kilometraža", mileage, FontAwesomeSolid.TACHOMETER_ALT);
    add(grid, BorderLayout.CENTER);

    add(
        Ui.hint("Procjene u Katalogu su informativne; stvarni trošak dolazi iz servisne evidencije."),
        BorderLayout.SOUTH);
  }

  /**
   * Dodaje jednu vizualno ujednačenu Dashboard karticu u mrežu.
   *
   * @param grid panel koji sadrži kartice
   * @param title naslov kartice
   * @param value labela čija će se vrijednost kasnije osvježavati
   * @param iconCode ikona kartice
   */
  private static void addCard(JPanel grid, String title, JLabel value, FontAwesomeSolid iconCode) {
    JPanel card = Ui.card();
    JPanel content = new JPanel(new GridLayout(3, 1, 0, 8));
    content.setOpaque(false);

    JLabel iconLabel = new JLabel(icon(iconCode, 34));
    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

    JLabel titleLabel = Ui.hint(title);
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

    value.setHorizontalAlignment(SwingConstants.CENTER);
    value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));

    content.add(iconLabel);
    content.add(titleLabel);
    content.add(value);
    card.add(content, BorderLayout.CENTER);
    grid.add(card);
  }

  /**
   * Stvara ikonu Dashboard kartice u zadanoj veličini.
   *
   * @param iconCode ikona iz FontAwesome skupa
   * @param size željena veličina ikone u pikselima
   * @return Swing ikona s bojom teksta trenutačne teme
   */
  private static FontIcon icon(FontAwesomeSolid iconCode, int size) {
    Color color = UIManager.getColor("Label.foreground");
    if (color == null) {
      color = Color.WHITE;
    }
    return FontIcon.of(iconCode, size, color);
  }

  /**
   * Popunjava Dashboard vrijednostima iz pripremljenog modela.
   *
   * @param dashboard sažetak aktivnog vozila
   */
  public void showDashboard(Dashboard dashboard) {
    total.setText(Ui.money(dashboard.getTotal()));

    MaintenanceRow next = dashboard.getNextMaintenance();
    if (next == null) {
      maintenance.setText("Nema praćenog održavanja");
    } else if (next.getRemainingRatio() <= 0) {
      maintenance.setText(next.getName() + " - potrebno obaviti");
    } else {
      maintenance.setText(next.getName() + " - za " + remaining(next));
    }

    problems.setText(Long.toString(dashboard.getOpenProblems()));
    mileage.setText(Ui.km(dashboard.getVehicle().getCurrentMileage()));
  }

  /**
   * Pretvara podatke najbližeg održavanja u kratki korisnički tekst.
   *
   * @param row najbliže održavanje ili {@code null}
   * @return tekst prikladan za Dashboard karticu
   */
  private static String remaining(MaintenanceRow row) {
    String result = "";

    if (row.getRemainingKm() != null) {
      result = Ui.km(row.getRemainingKm());
    }

    if (row.getRemainingDays() != null) {
      if (!result.isEmpty()) {
        result += " / ";
      }
      result += row.getRemainingDays() + " dana";
    }

    if (result.isEmpty()) {
      return "-";
    }
    return result;
  }
}
