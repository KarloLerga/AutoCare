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

  public String getColumnName(int column) {
    return new String[] {"Vrsta", "Rad", "Stvarno placeno (EUR)"}[column];
  }

  public Object getValueAt(int row, int column) {
    Line line = lines.get(row);
    if (column == 0) {
      return Ui.category(line.work.getCategory());
    }
    if (column == 1) {
      return line.work.getName();
    }
    return line.amount;
  }

  public boolean isCellEditable(int row, int column) {
    return column == 2;
  }

  public void setValueAt(Object value, int row, int column) {
    if (column == 2) {
      if (value == null) {
        lines.get(row).amount = "";
      } else {
        lines.get(row).amount = value.toString();
      }
      fireTableCellUpdated(row, column);
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
    List<ItemInput> inputs = new ArrayList<>();
    for (Line line : lines) {
      inputs.add(new ItemInput(line.work.getId(), Ui.parseMoney(line.amount, history)));
    }
    return inputs;
  }
}
