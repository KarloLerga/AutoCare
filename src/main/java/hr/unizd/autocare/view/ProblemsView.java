package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Problemi koje vlasnik želi zapamtiti za mehaničara. */
public final class ProblemsView extends JPanel {
  public final JComboBox<ProblemCategory> category = new JComboBox<>(ProblemCategory.values());
  public final JTextArea description = new JTextArea(3, 36);
  public final JButton add = Ui.button("Spremi problem");
  public final JTable table;

  private final DefaultTableModel tableModel;

  public ProblemsView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Kategorija", "Problem", "Status", "Datum"}) {
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

    JPanel editor = Ui.card();
    editor.add(Ui.heading("Novi problem"), BorderLayout.NORTH);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    category.setSelectedItem(ProblemCategory.OTHER);
    JPanel fields = Ui.form();
    Ui.field(fields, 0, "Kategorija (opcionalno)", category);
    Ui.field(fields, 1, "Što primjećujete", new JScrollPane(description));
    editor.add(fields, BorderLayout.CENTER);
    editor.add(Ui.actions(add), BorderLayout.SOUTH);

    JPanel top = Ui.column();
    top.add(Ui.heading("Problemi"));
    top.add(Ui.hint("Problem se označava riješenim prilikom spremanja servisa."));
    top.add(editor);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public ProblemCategory selectedCategory() {
    return (ProblemCategory) category.getSelectedItem();
  }

  public void setRows(List<Problem> values) {
    tableModel.setRowCount(0);
    for (Problem problem : values) {
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
}
