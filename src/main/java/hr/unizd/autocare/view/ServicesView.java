package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ItemRow;
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
  public final JButton add = Ui.button("Novi servis");
  public final JButton detail = Ui.button("Detalj");
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<ServiceRow> services = new ArrayList<>();

  public ServicesView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Datum", "Km", "Radovi", "Poznati trošak", "Napomena"}) {
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
            Ui.total(service.getTotal()),
            service.getNote()
          });
    }
  }

  public ServiceRow selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return services.get(table.convertRowIndexToModel(selectedRow));
  }

  public void showServiceDetails(ServiceDetail detail) {
    StringBuilder text = new StringBuilder();
    text.append(Ui.date(detail.getHeader().getDate()));
    text.append(" / ");
    text.append(Ui.km(detail.getHeader().getMileage()));
    text.append("\n\n");
    for (ItemRow item : detail.getItems()) {
      text.append(item.getName());
      text.append(": ");
      text.append(Ui.money(item.getActualPrice()));
      text.append("\n");
    }
    text.append("\nUkupno: ");
    text.append(Ui.total(detail.getHeader().getTotal()));
    text.append("\nNapomena: ");
    String note = detail.getHeader().getNote();
    if (note == null || note.isBlank()) {
      text.append("-");
    } else {
      text.append(note);
    }
    text.append("\n\nRiješene bilješke:\n");
    for (String problem : detail.getResolvedProblems()) {
      text.append(problem);
      text.append("\n");
    }

    JTextArea area = new JTextArea(text.toString(), 18, 65);
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
