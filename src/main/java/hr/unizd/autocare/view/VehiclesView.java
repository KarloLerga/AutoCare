package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Upravljanje vozilima; jedino mjesto promjene aktivnog vozila. */
public final class VehiclesView extends JPanel {
  public final JButton add = new JButton("Dodaj vozilo");
  public final JButton edit = new JButton("Promijeni kilometražu");
  public final JButton activate = new JButton("Aktiviraj");
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<Vehicle> vehicles = new ArrayList<>();

  public VehiclesView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Vozilo", "Godina", "Motor", "Kilometraža", "Aktivno"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    configureTable(table);
    JPanel top = Ui.column();
    top.add(Ui.heading("Vozila"));
    top.add(Ui.row(add, edit, activate));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public void setRows(List<Vehicle> values, Integer activeVehicleId) {
    vehicles = new ArrayList<>(values);
    tableModel.setRowCount(0);

    for (Vehicle vehicle : vehicles) {
      String active = "";
      if (activeVehicleId != null && vehicle.getId().equals(activeVehicleId)) {
        active = "Da";
      }

      tableModel.addRow(
          new Object[] {
            vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel(),
            vehicle.getProductionYear(),
            vehicle.getVariant().getEngineLabel(),
            Ui.km(vehicle.getCurrentMileage()),
            active
          });
    }
  }

  public Vehicle selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return vehicles.get(selectedRow);
  }

  private static void configureTable(JTable table) {
    table.setRowHeight(32);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
  }
}
