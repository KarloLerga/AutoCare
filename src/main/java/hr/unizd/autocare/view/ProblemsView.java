package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Dva statusa problema i ulaz u analizator. */
public final class ProblemsView extends JPanel {
  public final JComboBox<String> status = new JComboBox<>(new String[] {"Otvoreni", "Rijeseni"});
  public final JButton add = Ui.button("Analiziraj novi problem", true);
  public final JButton detail = Ui.button("Detalj", false);
  public final JTable table;
  private final DefaultTableModel tableModel;
  private List<ProblemRow> problems = new ArrayList<>();

  public ProblemsView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    tableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Opis simptoma", "Moguci uzrok", "Podudaranje %", "Procjena", "Datum"}) {
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
    top.add(Ui.heading("Problemi"));
    top.add(Ui.row(status, add, detail));
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.hint("Rjesavanje problema evidentira se kroz Novi servis, ne kroz sam rezultat analize."),
        BorderLayout.SOUTH);
  }

  public void setRows(List<ProblemRow> values) {
    problems = new ArrayList<>(values);
    tableModel.setRowCount(0);
    for (ProblemRow problem : problems) {
      tableModel.addRow(
          new Object[] {
            problem.getDescription(),
            problem.getSuggestion(),
            problem.getScore(),
            EstimateFormat.display(problem.getPrice()),
            problem.getCreatedAt().toLocalDate().toString()
          });
    }
  }

  public List<ProblemRow> rows() {
    return problems;
  }

  public ProblemRow selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return problems.get(table.convertRowIndexToModel(selectedRow));
  }

  public void showProblemDetails(ProblemRow problem) {
    Ui.info(
        this,
        problem.getDescription()
            + "\nMoguci uzrok: "
            + java.util.Objects.toString(problem.getSuggestion(), "Nema podudaranja")
            + "\nPodudaranje: "
            + java.util.Objects.toString(problem.getScore(), "-")
            + " %\nProcjena: "
            + EstimateFormat.display(problem.getPrice())
            + "\nIzvor: "
            + java.util.Objects.toString(problem.getPriceNote(), "Nepoznat")
            + "\nRijeseno servisom: "
            + java.util.Objects.toString(problem.getResolvedServiceId(), "-"));
  }
}
