package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Swing tablični prikaz izračunatih intervala održavanja aktivnog vozila. */
public class MaintenanceView extends JPanel {
  public final JTable table;

  private final DefaultTableModel tableModel;

  /** Stvara read-only tablicu održavanja i njezine stupce. */
  public MaintenanceView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Rad", "Zadnji datum", "Zadnji km", "Sljedeći datum", "Sljedeći km"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setRowHeight(32);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Održavanje"));
    top.add(Ui.hint("Prikazuju se održavanja koja su evidentirana kroz servisnu povijest."));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(Ui.hint("Informativne cijene i svi standardni zahvati nalaze se u Katalogu."), BorderLayout.SOUTH);
  }

  /**
   * Zamjenjuje sadržaj tablice aktualnim izračunima održavanja.
   *
   * @param values izračunati retci održavanja
   */
  public void setRows(List<MaintenanceRow> values) {
    tableModel.setRowCount(0);
    for (MaintenanceRow row : values) {
      tableModel.addRow(
          new Object[] {row.getName(),
            Ui.date(row.getLastDate()),
            Ui.km(row.getLastMileage()),
            Ui.date(row.getNextDate()),
            Ui.km(row.getNextMileage())
          });
    }
  }
}
