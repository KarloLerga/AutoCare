package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Cetiri kartice s kratkim sazetkom aktivnog vozila. */
public final class DashboardView extends JPanel {
  private final JLabel total = Ui.hint("-"),
      maintenance = Ui.hint("-"),
      problems = Ui.hint("-"),
      mileage = Ui.hint("-");

  public DashboardView() {
    super(new BorderLayout(16, 16));
    setOpaque(false);
    add(Ui.heading("Pregled vozila"), BorderLayout.NORTH);
    JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
    grid.setOpaque(false);
    String[] names = {
      "Poznati stvarni troskovi", "Odrzavanje", "Otvoreni problemi", "Trenutna kilometraza"
    };
    JLabel[] values = {total, maintenance, problems, mileage};
    for (int i = 0; i < 4; i++) {
      JPanel card = Ui.card();
      card.add(Ui.hint(names[i]), BorderLayout.NORTH);
      values[i].setFont(values[i].getFont().deriveFont(Font.BOLD, 19f));
      card.add(values[i]);
      grid.add(card);
    }
    add(grid, BorderLayout.CENTER);
    add(
        Ui.hint("Procjene su informativne. Nisu racun ni dijagnoza mehanicara."),
        BorderLayout.SOUTH);
  }

  public void show(Dashboard d) {
    total.setText(Ui.total(d.getTotal()));
    maintenance.setText(
        d.getCovered() == 0
            ? "Nema pravila za ovu varijantu"
            : d.getDue()
                + " dospjelo / "
                + d.getSoon()
                + " uskoro / "
                + d.getNoData()
                + " bez podataka");
    problems.setText(Long.toString(d.getOpenProblems()));
    mileage.setText(Ui.km(d.getVehicle().getMileage()));
  }
}
