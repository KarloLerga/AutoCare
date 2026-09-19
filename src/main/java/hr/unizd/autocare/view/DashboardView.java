package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/** Četiri jednostavne kartice sa sažetkom aktivnog vozila. */
public final class DashboardView extends JPanel {
  private final JLabel total = Ui.hint("-");
  private final JLabel maintenance = Ui.hint("-");
  private final JLabel notes = Ui.hint("-");
  private final JLabel mileage = Ui.hint("-");

  public DashboardView() {
    super(new BorderLayout(16, 16));
    setOpaque(false);
    add(Ui.heading("Pregled vozila"), BorderLayout.NORTH);
    JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
    grid.setOpaque(false);
    addCard(grid, "Ukupni stvarni troškovi", total, FontAwesomeSolid.EURO_SIGN);
    addCard(grid, "Sljedeće održavanje", maintenance, FontAwesomeSolid.WRENCH);
    addCard(grid, "Aktivne bilješke", notes, FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    addCard(grid, "Trenutna kilometraža", mileage, FontAwesomeSolid.TACHOMETER_ALT);
    add(grid, BorderLayout.CENTER);
    add(Ui.hint("Procjene u Katalogu su informativne; stvarni trošak dolazi iz servisne evidencije."), BorderLayout.SOUTH);
  }

  private static void addCard(
      JPanel grid, String title, JLabel value, FontAwesomeSolid iconCode) {
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

  private static FontIcon icon(FontAwesomeSolid iconCode, int size) {
    Color color = UIManager.getColor("Label.foreground");
    return FontIcon.of(iconCode, size, color == null ? Color.WHITE : color);
  }

  public void showDashboard(Dashboard dashboard) {
    total.setText(Ui.total(dashboard.getTotal()));
    MaintenanceRow next = dashboard.getNextMaintenance();
    if (next == null) {
      maintenance.setText("Nema praćenog održavanja");
    } else if (next.getStatus() == MaintenanceStatus.DUE) {
      maintenance.setText(
          "<html><div style='text-align:center;'>" + next.getName() + "<br>Dospjelo</div></html>");
    } else {
      String remaining = remaining(next, dashboard.getVehicle().getMileage());
      maintenance.setText(
          "<html><div style='text-align:center;'>"
              + next.getName()
              + "<br>za "
              + remaining
              + "</div></html>");
    }
    notes.setText(Long.toString(dashboard.getActiveNotes()));
    mileage.setText(Ui.km(dashboard.getVehicle().getMileage()));
  }

  private static String remaining(MaintenanceRow row, int currentMileage) {
    StringBuilder result = new StringBuilder();
    if (row.getNextMileage() != null) {
      int remainingKm = Math.max(0, row.getNextMileage() - currentMileage);
      result.append(Ui.km(remainingKm));
    }
    if (row.getNextDate() != null) {
      long remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), row.getNextDate()));
      if (result.length() > 0) {
        result.append(" / ");
      }
      if (remainingDays >= 60) {
        long months = Math.max(1, Math.round(remainingDays / 30.0));
        result.append(months).append(" mj.");
      } else {
        result.append(remainingDays).append(" dana");
      }
    }
    if (result.length() == 0) {
      return Ui.status(row.getStatus());
    }
    return result.toString();
  }
}
