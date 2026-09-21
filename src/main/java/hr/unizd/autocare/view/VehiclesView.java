package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.VehicleRow;
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
  public final JButton add = Ui.button("Dodaj vozilo");
  public final JButton edit = Ui.button("Promijeni kilometražu");
  public final JButton activate = Ui.button("Aktiviraj");
  public final JButton delete = Ui.button("Obriši");
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<VehicleRow> vehicles = new ArrayList<>();

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
    top.add(Ui.row(add, edit, activate, delete));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.hint("Brisanje uklanja i servisnu povijest i probleme odabranog vozila."),
        BorderLayout.SOUTH);
  }

  public void setRows(List<VehicleRow> values) {
    vehicles = new ArrayList<>(values);
    tableModel.setRowCount(0);
    for (VehicleRow vehicle : vehicles) {
      String active = "";
      if (vehicle.getActive()) {
        active = "Da";
      }

      tableModel.addRow(
          new Object[] {
            vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel(),
            vehicle.getYear(),
            vehicle.getVariant().getEngineLabel(),
            Ui.km(vehicle.getMileage()),
            active
          });
    }
  }

  public VehicleRow selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    int modelRow = table.convertRowIndexToModel(selectedRow);
    return vehicles.get(modelRow);
  }

  private static void configureTable(JTable table) {
    table.setRowHeight(32);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
  }
}
