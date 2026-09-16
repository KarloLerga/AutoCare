package hr.unizd.autocare.view.components;

import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.WorkRow;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Lokalni draft stavki. Promjena retka ne pise u bazu. */
public final class ServiceItemsModel extends AbstractTableModel {
  private static final class Line {
    final WorkRow work;
    String amount = "";

    Line(WorkRow work) {
      this.work = work;
    }
  }

  private final List<Line> lines = new ArrayList<>();

  public int getRowCount() {
    return lines.size();
  }

  public int getColumnCount() {
    return 3;
  }

  public String getColumnName(int c) {
    return new String[] {"Vrsta", "Rad", "Stvarno placeno (EUR)"}[c];
  }

  public Object getValueAt(int r, int c) {
    Line line = lines.get(r);
    return switch (c) {
      case 0 -> Ui.category(line.work.getCategory());
      case 1 -> line.work.getName();
      default -> line.amount;
    };
  }

  public boolean isCellEditable(int r, int c) {
    return c == 2;
  }

  public void setValueAt(Object value, int row, int col) {
    if (col == 2) {
      lines.get(row).amount = value == null ? "" : value.toString();
      fireTableCellUpdated(row, col);
    }
  }

  public void add(WorkRow work) {
    for (Line line : lines) {
      if (line.work.getId() == work.getId()) {
        throw new IllegalArgumentException("Rad je vec u servisu.");
      }
    }
    int row = lines.size();
    lines.add(new Line(work));
    fireTableRowsInserted(row, row);
  }

  public void remove(int row) {
    if (row >= 0 && row < lines.size()) {
      lines.remove(row);
      fireTableRowsDeleted(row, row);
    }
  }

  public List<ItemInput> snapshot(boolean history) {
    List<ItemInput> out = new ArrayList<>();
    for (Line line : lines) {
      out.add(new ItemInput(line.work.getId(), Ui.parseMoney(line.amount, history)));
    }
    return List.copyOf(out);
  }
}
