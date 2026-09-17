package hr.unizd.autocare.view.components;

import hr.unizd.autocare.model.Data.WorkRow;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Pretrazivanje kataloga radova kroz obicnu Swing tablicu. */
public final class WorkPicker {
  private WorkPicker() {}

  public static WorkRow choose(Component parent, List<WorkRow> values) {
    List<WorkRow> rows = new ArrayList<>(values);
    DefaultTableModel tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Rad", "Kategorija", "Procijenjena ukupna cijena", "Izvor / ogranicenje"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    for (WorkRow work : rows) {
      tableModel.addRow(
          new Object[] {
            work.getName(),
            Ui.category(work.getCategory()),
            EstimateFormat.display(work.getPrice()),
            work.getPriceNote()
          });
    }

    final JTable table = new JTable(tableModel);
    table.setRowHeight(30);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    final TableRowSorter<DefaultTableModel> sorter =
        new TableRowSorter<>(tableModel);
    table.setRowSorter(sorter);

    JTextField search = new JTextField();
    search.getDocument().addDocumentListener(
        new DocumentListener() {
          private void updateFilter() {
            String searchText = search.getText();
            if (searchText == null || searchText.isBlank()) {
              sorter.setRowFilter(null);
            } else {
              sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(searchText)));
            }
          }

          @Override
          public void insertUpdate(DocumentEvent event) {
            updateFilter();
          }

          @Override
          public void removeUpdate(DocumentEvent event) {
            updateFilter();
          }

          @Override
          public void changedUpdate(DocumentEvent event) {
            updateFilter();
          }
        });

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.add(search, BorderLayout.NORTH);
    panel.add(new JScrollPane(table), BorderLayout.CENTER);
    panel.setPreferredSize(new Dimension(760, 340));
    int answer =
        JOptionPane.showConfirmDialog(
            parent,
            panel,
            "Pretrazite i odaberite rad",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (answer != JOptionPane.OK_OPTION) {
      return null;
    }
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      throw new IllegalArgumentException("Nije odabran rad.");
    }
    return rows.get(table.convertRowIndexToModel(selectedRow));
  }
}
