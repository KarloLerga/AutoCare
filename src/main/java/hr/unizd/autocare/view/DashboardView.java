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
import javax.swing.UIManager;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/** Četiri bordered kartice s kratkim sažetkom aktivnog vozila. */
public final class DashboardView extends JPanel {
  private final JLabel total = Ui.hint("-");
  private final JLabel maintenance = Ui.hint("-");
  private final JLabel problems = Ui.hint("-");
  private final JLabel mileage = Ui.hint("-");

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
    add(Ui.hint("Procjene su informativne i nisu račun ni dijagnoza mehaničara."), BorderLayout.SOUTH);
  }

  private static void addCard(
      JPanel grid, String title, JLabel value, FontAwesomeSolid iconCode) {
    JPanel card = Ui.card();
    JPanel heading = new JPanel(new BorderLayout(8, 8));
    heading.setOpaque(false);
    heading.add(new JLabel(icon(iconCode, 24)), BorderLayout.WEST);
    heading.add(Ui.hint(title), BorderLayout.CENTER);
    card.add(heading, BorderLayout.NORTH);
    value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));
    card.add(value, BorderLayout.CENTER);
    grid.add(card);
  }

  private static FontIcon icon(FontAwesomeSolid iconCode, int size) {
    Color color = UIManager.getColor("Label.foreground");
    return FontIcon.of(iconCode, size, color == null ? Color.WHITE : color);
  }

  public void show(Dashboard dashboard) {
    total.setText(Ui.total(dashboard.getTotal()));
    MaintenanceRow next = dashboard.getNextMaintenance();
    if (next == null) {
      maintenance.setText("Nema praćenog održavanja");
    } else {
      StringBuilder value = new StringBuilder(next.getName());
      value.append(" — ").append(Ui.status(next.getStatus()));
      if (next.getNextDate() != null) {
        value.append(" / ").append(Ui.date(next.getNextDate()));
      }
      if (next.getNextMileage() != null) {
        value.append(" / ").append(Ui.km(next.getNextMileage()));
      }
      maintenance.setText("<html>" + value + "</html>");
    }
    problems.setText(Long.toString(dashboard.getOpenProblems()));
    mileage.setText(Ui.km(dashboard.getVehicle().getMileage()));
  }
}
