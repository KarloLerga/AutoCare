package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Bilješke vlasnika o stvarima koje želi zapamtiti za mehaničara. */
public final class ProblemsView extends JPanel {
  public final JComboBox<ProblemCategory> category =
      new JComboBox<>(ProblemCategory.values());
  public final JTextArea description = new JTextArea(3, 36);
  public final JButton add = Ui.button("Spremi bilješku");
  public final JButton close = Ui.button("Zatvori odabranu");
  public final JTable table;

  private final DefaultTableModel tableModel;
  private final List<ProblemRow> rows = new ArrayList<>();

  public ProblemsView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Kategorija", "Bilješka", "Status", "Datum"}) {
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

    JPanel editor = Ui.card();
    editor.add(Ui.heading("Nova bilješka"), BorderLayout.NORTH);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    category.setSelectedItem(ProblemCategory.OTHER);
    JPanel fields = Ui.form();
    Ui.field(fields, 0, "Kategorija", category);
    Ui.field(fields, 1, "Što primjećujete", new JScrollPane(description));
    editor.add(fields, BorderLayout.CENTER);
    editor.add(Ui.actions(add), BorderLayout.SOUTH);

    JPanel top = Ui.column();
    top.add(Ui.heading("Bilješke"));
    top.add(Ui.hint("Zapišite što primjećujete bez pokušaja dijagnosticiranja kvara."));
    top.add(editor);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.row(
            close,
            Ui.hint(
                "Bilješku možete zatvoriti ovdje ili je označiti riješenom prilikom spremanja servisa.")),
        BorderLayout.SOUTH);
  }

  public ProblemCategory selectedCategory() {
    return (ProblemCategory) category.getSelectedItem();
  }

  public ProblemRow selectedRow() {
    int selected = table.getSelectedRow();
    if (selected < 0) {
      return null;
    }
    return rows.get(table.convertRowIndexToModel(selected));
  }

  public void setRows(List<ProblemRow> values) {
    rows.clear();
    rows.addAll(values);
    tableModel.setRowCount(0);
    for (ProblemRow problem : values) {
      tableModel.addRow(
          new Object[] {
            problem.getCategory(),
            problem.getDescription(),
            Ui.problemStatus(problem.getStatus()),
            Ui.date(problem.getCreatedAt().toLocalDate())
          });
    }
  }

  public void clearEditor() {
    category.setSelectedItem(ProblemCategory.OTHER);
    description.setText("");
  }

  public boolean selectedIsOpen() {
    ProblemRow selected = selectedRow();
    return selected != null && selected.getStatus() == ProblemStatus.OPEN;
  }
}
