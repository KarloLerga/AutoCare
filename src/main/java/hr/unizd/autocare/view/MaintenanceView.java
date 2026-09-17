package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.MaintenanceEstimate;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Praćeno održavanje i procjena odabrane konkretne stavke. */
public final class MaintenanceView extends JPanel {
  public final JComboBox<MaintenanceRow> work = new JComboBox<>();
  public final JButton estimate = Ui.button("Procijeni cijenu i interval");
  public final JLabel estimatePrice = Ui.hint("Cijena: -");
  public final JLabel estimateInterval = Ui.hint("Interval: -");
  public final JLabel coverage = Ui.hint("Učitavanje održavanja...");
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
              "Rad", "Zadnji datum", "Zadnji km", "Sljedeći datum", "Sljedeći km", "Status"
            }) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setRowHeight(32);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Održavanje"));
    top.add(coverage);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel estimator = Ui.column();
    estimator.add(Ui.row(new JLabel("Praćeni rad:"), work, estimate));
    estimator.add(Ui.row(estimatePrice, estimateInterval));
    add(estimator, BorderLayout.SOUTH);
  }

  public void setRows(List<MaintenanceRow> values) {
    maintenanceRows = new ArrayList<>(values);
    tableModel.setRowCount(0);
    for (MaintenanceRow row : maintenanceRows) {
      tableModel.addRow(
          new Object[] {
            row.getName(),
            Ui.date(row.getLastDate()),
            Ui.km(row.getLastMileage()),
            Ui.date(row.getNextDate()),
            Ui.km(row.getNextMileage()),
            Ui.status(row.getStatus())
          });
    }
    work.setModel(new DefaultComboBoxModel<>(maintenanceRows.toArray(new MaintenanceRow[0])));
    if (maintenanceRows.isEmpty()) {
      estimatePrice.setText("Cijena: -");
      estimateInterval.setText("Interval: -");
    }
  }

  public List<MaintenanceRow> rows() {
    return maintenanceRows;
  }

  public MaintenanceRow selectedWork() {
    return (MaintenanceRow) work.getSelectedItem();
  }

  public void showEstimate(MaintenanceEstimate estimate) {
    estimatePrice.setText("Cijena: " + Ui.estimate(estimate.getEstimatedPrice()));
    String interval = "Interval: ";
    if (estimate.getIntervalKm() != null) {
      interval += Ui.km(estimate.getIntervalKm());
    }
    if (estimate.getIntervalMonths() != null) {
      if (estimate.getIntervalKm() != null) {
        interval += " / ";
      }
      interval += estimate.getIntervalMonths() + " mjeseci";
    }
    interval += " / sljedeće: " + Ui.date(estimate.getNextDate());
    if (estimate.getNextMileage() != null) {
      interval += " / " + Ui.km(estimate.getNextMileage());
    }
    estimateInterval.setText(interval);
  }
}
