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

/** Swing prikaz svih vozila prijavljenog korisnika i osnovnih akcija nad odabranim vozilom. */
public class VehiclesView extends JPanel {
  /** Otvara dijalog za unos vozila prijavljenog korisnika. */
  public final JButton add = new JButton("Dodaj vozilo");

  /** Otvara uređivanje kilometraže trenutno označenog vozila. */
  public final JButton edit = new JButton("Promijeni kilometražu");

  /** Postavlja označeno vozilo kao trenutačno aktivno. */
  public final JButton activate = new JButton("Aktiviraj");

  /** Read-only tablica vozila korisnika; odabir određuje akcije uređivanja i aktiviranja. */
  public final JTable table;

  /** Model koji prikazuje osnovne podatke i odbija izravno uređivanje ćelija. */
  private final DefaultTableModel tableModel;

  /** Kopija vozila redom prikazanih u tablici, korištena za dohvat odabranog objekta. */
  private List<Vehicle> vehicles = new ArrayList<>();

  /** Stvara tablicu vozila i gumbe za dodavanje, promjenu kilometraže i aktiviranje. */
  public VehiclesView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Vozilo", "Godina", "Motor", "Kilometraža", "Aktivno"}) {
          /** Ostavlja retke vozila samo za čitanje; izmjene prolaze kroz formu i VehicleService. */
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

  /**
   * Zamjenjuje prikazane retke aktualnim vozilima i označava koje je vozilo aktivno.
   *
   * @param values vozila korisnika
   * @param activeVehicleId ID aktivnog vozila ili {@code null}
   */
  public void setRows(List<Vehicle> values, Integer activeVehicleId) {
    vehicles = new ArrayList<>(values);
    tableModel.setRowCount(0);

    for (Vehicle vehicle : vehicles) {
      String active = "";
      if (activeVehicleId != null && vehicle.getId().equals(activeVehicleId)) {
        active = "Da";
      }

      tableModel.addRow(
          new Object[] {vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel(),
            vehicle.getProductionYear(),
            vehicle.getVariant().getEngineLabel(),
            Ui.km(vehicle.getCurrentMileage()),
            active
          });
    }
  }

  /** Dohvaća odabrani redak kako bi Controller znao na kojem vozilu izvršiti akciju.
   *
   * @return vozilo označeno u tablici ili {@code null} ako nema odabira
   */
  public Vehicle selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return vehicles.get(selectedRow);
  }

  /**
   * Primjenjuje zajedničke postavke čitljivosti i odabira na tablicu vozila.
   *
   * @param table tablica koju treba konfigurirati
   */
  private static void configureTable(JTable table) {
    table.setRowHeight(32);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
  }
}
