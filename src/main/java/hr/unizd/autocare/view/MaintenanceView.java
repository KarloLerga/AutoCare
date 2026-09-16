package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.DataTable;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

/** Izvedeni raspored i informativna procjena; ovaj ekran nikad ne sprema servis. */
public final class MaintenanceView extends JPanel {
  public final JButton estimate = Ui.button("Procijeni odabrana odrzavanja", false);
  public final JLabel coverage = Ui.hint("Ucitajte odrzavanje.");
  public final DataTable<MaintenanceRow> table =
      new DataTable<>(
          new String[] {
            "Rad",
            "Zadnji datum",
            "Zadnji km",
            "Sljedeci datum",
            "Sljedeci km",
            "Status",
            "Preostalo",
            "Izvor intervala"
          },
          (m, c) ->
              switch (c) {
                case 0 -> m.getName();
                case 1 -> m.getLastDate();
                case 2 -> m.getLastMileage();
                case 3 -> m.getNextDate();
                case 4 -> m.getNextMileage();
                case 5 -> Ui.status(m.getStatus());
                case 6 -> remaining(m);
                default -> m.getIntervalSource();
              });

  private static String remaining(MaintenanceRow row) {
    String km = row.getRemainingKm() == null ? null : Ui.km(row.getRemainingKm());
    String days = row.getRemainingDays() == null ? null : row.getRemainingDays() + " dana";
    if (km == null && days == null) {
      return "Nema podataka";
    }
    return km == null ? days : days == null ? km : km + " / " + days;
  }

  public MaintenanceView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    JPanel top = Ui.column();
    top.add(Ui.heading("Odrzavanje"));
    top.add(coverage);
    add(top, BorderLayout.NORTH);
    add(table);
    table.table().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    add(Ui.row(estimate), BorderLayout.SOUTH);
  }
}
