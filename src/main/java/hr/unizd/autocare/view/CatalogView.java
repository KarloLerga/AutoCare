package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.model.Data.CatalogRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Pretraživi informativni cjenik standardnih zahvata za aktivno vozilo. */
public final class CatalogView extends JPanel {
  public final JTextField search = new JTextField(24);
  public final JComboBox<String> category = new JComboBox<>();
  public final JButton searchButton = Ui.button("Pretraži");
  public final JLabel resultInfo = Ui.hint(" ");
  public final JTable table;

  private final DefaultTableModel tableModel;

  public CatalogView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);

    category.addItem("Sve kategorije");
    for (CatalogCategory value : CatalogCategory.values()) {
      category.addItem(value.getDisplayName());
    }

    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Zahvat", "Kategorija", "Vrsta", "Okvirna cijena", "Interval"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setRowHeight(32);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Katalog"));
    top.add(
        Ui.hint(
            "Informativni rasponi cijena standardnih zahvata za cjenovnu klasu aktivnog vozila."));
    top.add(
        Ui.row(
            new JLabel("Pretraži:"),
            search,
            new JLabel("Kategorija:"),
            category,
            searchButton));
    top.add(resultInfo);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.hint("Procjena nije dijagnoza niti stvarni račun. Stvarna cijena sprema se tek u Servisima."),
        BorderLayout.SOUTH);
  }

  public CatalogCategory selectedCategory() {
    int index = category.getSelectedIndex();
    return index <= 0 ? null : CatalogCategory.values()[index - 1];
  }

  public void setRows(List<CatalogRow> rows) {
    tableModel.setRowCount(0);
    for (CatalogRow row : rows) {
      tableModel.addRow(
          new Object[] {
            row.getName(),
            row.getCatalogCategory(),
            Ui.workCategory(row.getWorkCategory()),
            Ui.priceRange(row.getMinPrice(), row.getMaxPrice()),
            interval(row)
          });
    }
    resultInfo.setText("Prikazano zahvata: " + rows.size());
  }

  private static String interval(CatalogRow row) {
    if (row.getWorkCategory() == hr.unizd.autocare.domain.WorkCategory.REPAIR) {
      return "-";
    }
    StringBuilder result = new StringBuilder();
    if (row.getIntervalKm() != null) {
      result.append(Ui.km(row.getIntervalKm()));
    }
    if (row.getIntervalMonths() != null) {
      if (result.length() > 0) {
        result.append(" / ");
      }
      result.append(row.getIntervalMonths()).append(" mj.");
    }
    return result.length() == 0 ? "-" : result.toString();
  }
}
