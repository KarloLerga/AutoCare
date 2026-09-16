package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.view.components.DataTable;
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/** Dva statusa problema i ulaz u analizator. */
public final class ProblemsView extends JPanel {
  public final JComboBox<String> status = new JComboBox<>(new String[] {"Otvoreni", "Rijeseni"});
  public final JButton add = Ui.button("Analiziraj novi problem", true),
      detail = Ui.button("Detalj", false);
  public final DataTable<ProblemRow> table =
      new DataTable<>(
          new String[] {"Opis simptoma", "Moguci uzrok", "Podudaranje %", "Procjena", "Datum"},
          (p, c) ->
              switch (c) {
                case 0 -> p.getDescription();
                case 1 -> p.getSuggestion();
                case 2 -> p.getScore();
                case 3 -> EstimateFormat.display(p.getPrice());
                default -> p.getCreatedAt().toLocalDate();
              });

  public ProblemsView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    JPanel top = Ui.column();
    top.add(Ui.heading("Problemi"));
    top.add(Ui.row(status, add, detail));
    add(top, BorderLayout.NORTH);
    add(table);
    add(
        Ui.hint(
            "Rjesavanje problema evidentira se kroz Novi servis, ne kroz sam rezultat analize."),
        BorderLayout.SOUTH);
  }
}
