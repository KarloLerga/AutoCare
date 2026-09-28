package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
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

/** Swing prikaz informativnog kataloga standardnih radova s pretragom i filtrom kategorije. */
public class CatalogView extends JPanel {
  /** Tekst koji Controller koristi za naziv rada pretraživan bez obzira na velika slova. */
  public final JTextField search = new JTextField(24);

  /** Kategorijski filtar; prva stavka predstavlja prikaz svih kategorija. */
  public final JComboBox<String> category = new JComboBox<>();

  /** Pokreće ponovno učitavanje kataloga s trenutačnim filtrima. */
  public final JButton searchButton = new JButton("Pretraži");

  /** Read-only tablica informativnih standardnih radova i njihovih intervala/cijena. */
  public final JTable table;

  /** Model tablice koji puni {@link #setRows(List)} i odbija uređivanje ćelija. */
  private final DefaultTableModel tableModel;

  /** Stvara kontrolu pretrage, odabir kategorije i read-only tablicu kataloga. */
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
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Katalog"));
    top.add(Ui.hint("Informativni rasponi cijena standardnih zahvata."));
    top.add(Ui.row(new JLabel("Pretraži:"), search, new JLabel("Kategorija:"), category, searchButton));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.hint("Procjena nije dijagnoza niti stvarni račun. Stvarna cijena sprema se tek u Servisima."),
        BorderLayout.SOUTH);
  }

  /** @return trenutno odabrana kategorija kataloga ili {@code null} kada se prikazuju sve kategorije */
  public CatalogCategory selectedCategory() {
    int index = category.getSelectedIndex();
    if (index <= 0) {
      return null;
    }
    return CatalogCategory.values()[index - 1];
  }

  /**
   * Prikazuje zadani skup standardnih radova u tablici kataloga.
   *
   * @param works radovi nakon primjene filtra
   */
  public void setRows(List<WorkDefinition> works) {
    tableModel.setRowCount(0);
    for (WorkDefinition work : works) {
      tableModel.addRow(
          new Object[] {work.getName(),
            work.getCatalogCategory(),
            Ui.workCategory(work.getCategory()),
            Ui.priceRange(work.getMinPrice(), work.getMaxPrice()),
            interval(work)
          });
    }
  }

  /**
   * Pretvara kilometarski i vremenski interval rada u kratki tekst za tablicu.
   *
   * @param work definicija rada
   * @return formatirani interval ili oznaka da interval nije definiran
   */
  private static String interval(WorkDefinition work) {
    if (work.getCategory() == WorkCategory.REPAIR) {
      return "-";
    }

    String result = "";
    if (work.getIntervalKm() != null) {
      result = Ui.km(work.getIntervalKm());
    }
    if (work.getIntervalMonths() != null) {
      if (!result.isEmpty()) {
        result += " / ";
      }
      result += work.getIntervalMonths() + " mj.";
    }
    if (result.isEmpty()) {
      return "-";
    }
    return result;
  }
}
