package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Izvedeni raspored i informativna procjena; ovaj ekran nikad ne sprema servis. */
public final class MaintenanceView extends JPanel {
  public final JButton estimate = Ui.button("Procijeni odabrana odrzavanja", false);
  public final JLabel coverage = Ui.hint("Ucitajte odrzavanje.");
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<MaintenanceRow> maintenanceRows = new ArrayList<>();

  public MaintenanceView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {
              "Rad",
              "Zadnji datum",
              "Zadnji km",
              "Sljedeci datum",
              "Sljedeci km",
              "Status",
              "Preostalo",
              "Izvor intervala"
            }) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setRowHeight(32);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    JPanel top = Ui.column();
    top.add(Ui.heading("Odrzavanje"));
    top.add(coverage);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(Ui.row(estimate), BorderLayout.SOUTH);
  }

  public void setRows(List<MaintenanceRow> values) {
    maintenanceRows = new ArrayList<>(values);
    tableModel.setRowCount(0);
    for (MaintenanceRow maintenance : maintenanceRows) {
      tableModel.addRow(
          new Object[] {
            maintenance.getName(),
            Ui.date(maintenance.getLastDate()),
            Ui.km(maintenance.getLastMileage()),
            Ui.date(maintenance.getNextDate()),
            Ui.km(maintenance.getNextMileage()),
            Ui.status(maintenance.getStatus()),
            remaining(maintenance),
            maintenance.getIntervalSource()
          });
    }
  }

  public List<MaintenanceRow> rows() {
    return maintenanceRows;
  }

  private static String remaining(MaintenanceRow row) {
    String remainingKm = row.getRemainingKm() == null ? null : Ui.km(row.getRemainingKm());
    String remainingDays =
        row.getRemainingDays() == null ? null : row.getRemainingDays() + " dana";
    if (remainingKm == null && remainingDays == null) {
      return "Nema podataka";
    }
    if (remainingKm == null) {
      return remainingDays;
    }
    if (remainingDays == null) {
      return remainingKm;
    }
    return remainingKm + " / " + remainingDays;
  }
}
