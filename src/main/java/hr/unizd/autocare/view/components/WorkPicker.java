package hr.unizd.autocare.view.components;

import hr.unizd.autocare.model.Data.WorkRow;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Pretrazivanje razumno malog kataloga radova; bez velkeho JComboBoxu. */
public final class WorkPicker {
  private WorkPicker() {}

  public static WorkRow choose(Component parent, List<WorkRow> rows) {
    DataTable<WorkRow> table =
        new DataTable<>(
            new String[] {"Rad", "Kategorija", "Procijenjena ukupna cijena", "Izvor / ogranicenje"},
            (w, c) ->
                switch (c) {
                  case 0 -> w.getName();
                  case 1 -> Ui.category(w.getCategory());
                  case 2 -> EstimateFormat.display(w.getPrice());
                  default -> w.getPriceNote();
                });
    table.setRows(rows);
    JTextField search = new JTextField();
    search
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              private void update() {
                table.filter(search.getText());
              }

              public void insertUpdate(DocumentEvent e) {
                update();
              }

              public void removeUpdate(DocumentEvent e) {
                update();
              }

              public void changedUpdate(DocumentEvent e) {
                update();
              }
            });
    JPanel p = new JPanel(new BorderLayout(8, 8));
    p.add(search, BorderLayout.NORTH);
    p.add(table, BorderLayout.CENTER);
    p.setPreferredSize(new Dimension(760, 340));
    int answer =
        JOptionPane.showConfirmDialog(
            parent,
            p,
            "Pretrazite i odaberite rad",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (answer != JOptionPane.OK_OPTION) {
      return null;
    }
    WorkRow selected = table.selected();
    if (selected == null) {
      throw new IllegalArgumentException("Nije odabran rad.");
    }
    return selected;
  }
}
