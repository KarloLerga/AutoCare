package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ProblemEstimate;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Ručni unos problema; otvoreni i riješeni problemi prikazani su zajedno. */
public final class ProblemsView extends JPanel {
  public final JTextArea description = new JTextArea(3, 36);
  public final JComboBox<WorkRow> repair = new JComboBox<>();
  public final JButton estimate = Ui.button("Procijeni cijenu");
  public final JButton add = Ui.button("Spremi problem");
  public final JLabel estimateLabel = Ui.hint("Procjena: -");
  public final JTable table;

  private final DefaultTableModel tableModel;

  public ProblemsView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Opis problema", "Odabrani popravak", "Procjena", "Status", "Datum"}) {
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

    JPanel editor = Ui.card();
    editor.add(Ui.heading("Novi problem"), BorderLayout.NORTH);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    JPanel fields = Ui.form();
    Ui.field(fields, 0, "Opis problema", new JScrollPane(description));
    Ui.field(fields, 1, "Mogući popravak", repair);
    editor.add(fields, BorderLayout.CENTER);
    JPanel actions = Ui.column();
    actions.add(Ui.row(estimate, add));
    actions.add(estimateLabel);
    editor.add(actions, BorderLayout.SOUTH);

    JPanel top = Ui.column();
    top.add(Ui.heading("Problemi"));
    top.add(editor);
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(Ui.hint("Problem se označava riješenim kroz stvarni servis."), BorderLayout.SOUTH);
    repair.setPreferredSize(new Dimension(360, repair.getPreferredSize().height));
  }

  public void setRepairs(List<WorkRow> values) {
    repair.setModel(new DefaultComboBoxModel<>(values.toArray(new WorkRow[0])));
    repair.setSelectedIndex(-1);
  }

  public WorkRow selectedRepair() {
    return (WorkRow) repair.getSelectedItem();
  }

  public void setRows(List<ProblemRow> values) {
    tableModel.setRowCount(0);
    for (ProblemRow problem : values) {
      tableModel.addRow(
          new Object[] {
            problem.getDescription(),
            problem.getSuggestedRepair() == null ? "-" : problem.getSuggestedRepair(),
            Ui.estimate(problem.getEstimatedCost()),
            Ui.problemStatus(problem.getStatus()),
            Ui.date(problem.getCreatedAt().toLocalDate())
          });
    }
  }

  public void showEstimate(ProblemEstimate value) {
    estimateLabel.setText(
        "Procjena: " + value.getWorkName() + " / " + Ui.estimate(value.getEstimatedCost()));
  }

  public void clearEditor() {
    description.setText("");
    repair.setSelectedIndex(-1);
    estimateLabel.setText("Procjena: -");
  }

}
