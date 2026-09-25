package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.model.Data.ServiceDetail;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Servisna povijest aktivnog vozila. */
public final class ServicesView extends JPanel {
  public final JButton add = new JButton("Novi servis");
  public final JButton detail = new JButton("Detalj");
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<ServiceRow> services = new ArrayList<>();

  public ServicesView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Datum", "Km", "Radovi", "Stvarni trošak", "Napomena"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setRowHeight(32);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Servisna povijest"));
    top.add(Ui.row(add, detail));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public void setRows(List<ServiceRow> values) {
    services = new ArrayList<>(values);
    tableModel.setRowCount(0);
    for (ServiceRow service : services) {
      tableModel.addRow(
          new Object[] {
            Ui.date(service.getDate()),
            service.getMileage(),
            service.getNames(),
            Ui.money(service.getTotal()),
            service.getNote()
          });
    }
  }

  public ServiceRow selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return services.get(selectedRow);
  }

  public void showServiceDetails(ServiceDetail detail) {
    String text = Ui.date(detail.getHeader().getDate());
    text += " / " + Ui.km(detail.getHeader().getMileage());
    text += "\n\n";

    for (ServiceItem item : detail.getItems()) {
      text += item.getWork().getName() + ": " + Ui.money(item.getActualPrice()) + "\n";
    }

    text += "\nUkupno: " + Ui.money(detail.getHeader().getTotal());
    text += "\nNapomena: ";

    String note = detail.getHeader().getNote();
    if (note == null || note.isBlank()) {
      text += "-";
    } else {
      text += note;
    }

    text += "\n\nRiješeni problemi:\n";
    for (String problem : detail.getResolvedProblems()) {
      text += problem + "\n";
    }

    JTextArea area = new JTextArea(text, 18, 65);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    JOptionPane.showMessageDialog(
        this,
        new JScrollPane(area),
        "Detalj servisa",
        JOptionPane.INFORMATION_MESSAGE);
  }
}
