package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.view.components.DataTable;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Paginirana povijest; detalj zasebno, bez ucitavanja svih tablica. */
public final class ServicesView extends JPanel {
  public final JButton add = Ui.button("Novi servis", true),
      detail = Ui.button("Detalj", false),
      previous = Ui.button("Prethodna", false),
      next = Ui.button("Sljedeca", false);
  public final JLabel page = Ui.hint("1");
  public final DataTable<ServiceRow> table =
      new DataTable<>(
          new String[] {"Datum", "Km", "Radovi", "Poznati trosak", "Napomena"},
          (s, c) ->
              switch (c) {
                case 0 -> s.getDate();
                case 1 -> s.getMileage();
                case 2 -> s.getNames();
                case 3 -> Ui.total(s.getTotal());
                default -> s.getNote();
              });

  public ServicesView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    JPanel top = Ui.column();
    top.add(Ui.heading("Servisna povijest"));
    top.add(Ui.row(add, detail));
    add(top, BorderLayout.NORTH);
    add(table);
    add(Ui.row(previous, page, next), BorderLayout.SOUTH);
  }
}
