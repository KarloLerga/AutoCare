package hr.unizd.autocare.view;

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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Prikaz otvorenih i rijesenih problema vozila. */
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
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);

    JPanel top = Ui.column();
    top.add(Ui.heading("Problemi"));
    top.add(Ui.row(status, add, detail));

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(
        Ui.hint("Problem se oznacava rijesenim kroz stvarni servis."),
        BorderLayout.SOUTH);
  }

  public void setRows(List<ProblemRow> values) {
    problems = new ArrayList<>(values);
    tableModel.setRowCount(0);

    for (ProblemRow problem : problems) {
      String suggestion = problem.getSuggestion();
      if (suggestion == null) {
        suggestion = "Nema podudaranja";
      }

      String score = "-";
      if (problem.getScore() != null) {
        score = problem.getScore().toPlainString();
      }

      tableModel.addRow(
          new Object[] {
            problem.getDescription(),
            suggestion,
            score,
            Ui.estimate(problem.getPrice()),
            Ui.date(problem.getCreatedAt().toLocalDate())
          });
    }
  }

  public ProblemRow selected() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }

    int modelRow = table.convertRowIndexToModel(selectedRow);
    return problems.get(modelRow);
  }

  public void showProblemDetails(ProblemRow problem) {
    String suggestion = problem.getSuggestion();
    if (suggestion == null) {
      suggestion = "Nema podudaranja";
    }

    String score = "-";
    if (problem.getScore() != null) {
      score = problem.getScore().toPlainString() + " %";
    }

    String source = problem.getPriceNote();
    if (source == null || source.isBlank()) {
      source = "Nepoznat";
    }

    String resolvedService = "-";
    if (problem.getResolvedServiceId() != null) {
      resolvedService = problem.getResolvedServiceId().toString();
    }

    String text =
        problem.getDescription()
            + "\nMoguci uzrok: "
            + suggestion
            + "\nPodudaranje: "
            + score
            + "\nProcjena: "
            + Ui.estimate(problem.getPrice())
            + "\nIzvor: "
            + source
            + "\nRijeseno servisom: "
            + resolvedService;

    Ui.info(this, text);
  }
}
