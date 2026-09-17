package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.MaintenanceEstimate;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
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

/** Praćeno održavanje i zasebna procjena bilo kojeg dostupnog održavanja. */
public final class MaintenanceView extends JPanel {
  public final JComboBox<WorkRow> work = new JComboBox<>();
  public final JButton estimate = Ui.button("Procijeni cijenu i interval");
  public final JLabel estimatePrice = Ui.hint("Cijena: -");
  public final JLabel estimateInterval = Ui.hint("Interval: -");
  public final JLabel coverage = Ui.hint("Učitavanje održavanja...");
  public final JTable table;

  private final DefaultTableModel tableModel;
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
    estimator.add(Ui.row(new JLabel("Održavanje:"), work, estimate));
    estimator.add(Ui.row(estimatePrice, estimateInterval));
    add(estimator, BorderLayout.SOUTH);
  }

  public void setRows(List<MaintenanceRow> values) {
    tableModel.setRowCount(0);
    for (MaintenanceRow row : values) {
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
  }

  public void setAvailableWorks(List<WorkRow> values) {
    work.setModel(new DefaultComboBoxModel<>(values.toArray(new WorkRow[0])));
    work.setSelectedIndex(-1);
    estimatePrice.setText("Cijena: -");
    estimateInterval.setText("Interval: -");
  }

  public WorkRow selectedWork() {
    return (WorkRow) work.getSelectedItem();
  }

  public void showEstimate(MaintenanceEstimate value) {
    estimatePrice.setText("Cijena: " + Ui.estimate(value.getEstimatedPrice()));
    StringBuilder interval = new StringBuilder("Interval: ");
    if (value.getIntervalKm() != null) {
      interval.append(Ui.km(value.getIntervalKm()));
    }
    if (value.getIntervalMonths() != null) {
      if (value.getIntervalKm() != null) {
        interval.append(" / ");
      }
      interval.append(value.getIntervalMonths()).append(" mjeseci");
    }
    estimateInterval.setText(interval.toString());
  }
}
