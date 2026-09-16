package hr.unizd.autocare.view.components;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.BiFunction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

/** Mala genericka read-only tablica. Indeks prikaza se uvijek pretvara u indeks modela. */
public final class DataTable<T> extends JPanel {
  private List<T> rows = List.of();
  private final String[] columns;
  private final BiFunction<T, Integer, Object> value;
  private final AbstractTableModel model;
  private final JTable table;

  public DataTable(String[] columns, BiFunction<T, Integer, Object> value) {
    super(new BorderLayout());
    this.columns = columns.clone();
    this.value = value;
    model =
        new AbstractTableModel() {
          public int getRowCount() {
            return rows.size();
          }

          public int getColumnCount() {
            return columns.length;
          }

          public String getColumnName(int c) {
            return columns[c];
          }

          public Object getValueAt(int r, int c) {
            return value.apply(rows.get(r), c);
          }

          public Class<?> getColumnClass(int c) {
            for (T row : rows) {
              Object v = value.apply(row, c);
              if (v != null) {
                return v.getClass();
              }
            }
            return Object.class;
          }
        };
    table =
        new JTable(model) {
          @Override
          public String getToolTipText(java.awt.event.MouseEvent event) {
            int row = rowAtPoint(event.getPoint()), column = columnAtPoint(event.getPoint());
            if (row < 0 || column < 0) {
              return null;
            }
            Object value = getValueAt(row, column);
            if (value == null) {
              return null;
            }
            String escaped =
                String.valueOf(value)
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
            return "<html><div style='width:360px'>" + escaped + "</div></html>";
          }
        };
    table.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          {
            putClientProperty("html.disable", Boolean.TRUE);
          }

          @Override
          protected void setValue(Object value) {
            super.setValue(value instanceof java.time.LocalDate date ? Ui.date(date) : value);
          }
        });
    table.setRowHeight(32);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public void setRows(List<T> values) {
    rows = List.copyOf(values);
    model.fireTableDataChanged();
  }

  public List<T> rows() {
    return rows;
  }

  public T selected() {
    int index = table.getSelectedRow();
    return index < 0 ? null : rows.get(table.convertRowIndexToModel(index));
  }

  public JTable table() {
    return table;
  }

  public void filter(String text) {
    TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();
    sorter.setRowFilter(
        text == null || text.isBlank()
            ? null
            : RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(text)));
  }
}
