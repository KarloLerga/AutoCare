package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.DiagnosticResult;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Dijalog za unos simptoma i prikaz rezultata analize. */
public final class AnalysisDialog extends JDialog {
  public final JTextArea description = new JTextArea(5, 45);
  public final JButton analyze = Ui.button("Analiziraj");
  public final JButton save = Ui.button("Spremi problem");
  public final JButton cancel = Ui.button("Zatvori");
  public final JLabel estimate = Ui.hint("Rezultat analize jos nije izracunat.");
  public final JTable results;

  private final DefaultTableModel resultTableModel;

  public AnalysisDialog(Window owner) {
    super(owner, "Analiza simptoma", ModalityType.APPLICATION_MODAL);

    setSize(900, 680);
    setLocationRelativeTo(owner);

    resultTableModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {
              "Moguci uzrok / popravak",
              "Podudaranje %",
              "Informativna procjena",
              "Izvor / ogranicenje"
            }) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    results = new JTable(resultTableModel);
    results.setRowHeight(32);
    results.setFillsViewportHeight(true);
    results.getTableHeader().setReorderingAllowed(false);

    description.setLineWrap(true);
    description.setWrapStyleWord(true);

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JPanel top = Ui.column();
    top.add(Ui.hint("Opisite simptome. Podudaranje je rezultat pravila, nije vjerojatnost kvara."));
    top.add(new JScrollPane(description));
    top.add(Ui.row(analyze));

    JPanel bottom = Ui.column();
    bottom.add(estimate);
    bottom.add(Ui.actions(cancel, save));

    root.add(top, BorderLayout.NORTH);
    root.add(new JScrollPane(results), BorderLayout.CENTER);
    root.add(bottom, BorderLayout.SOUTH);

    setContentPane(root);
    save.setEnabled(false);
  }

  public void setResults(List<DiagnosticResult> values) {
    resultTableModel.setRowCount(0);

    for (DiagnosticResult result : values) {
      resultTableModel.addRow(
          new Object[] {
            result.getCandidateName(),
            result.getScore(),
            Ui.estimate(result.getPrice()),
            result.getPriceNote()
          });
    }
  }
}
