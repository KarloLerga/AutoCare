package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.view.components.DateField;
import hr.unizd.autocare.view.components.ServiceItemsModel;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

/** Lokalna servisna forma; procijenjene cijene se ne kopiraju u stupac stvarnih cijena. */
public final class ServiceEditorDialog extends JDialog {
  public final DateField date = new DateField(LocalDate.now());
  public final JSpinner mileage;
  public final JTextArea note = new JTextArea(3, 25);
  public final ServiceItemsModel items = new ServiceItemsModel();
  public final JTable itemTable = new JTable(items);
  public final JButton addMaintenance = Ui.button("Dodaj odrzavanje", false),
      addRepair = Ui.button("Dodaj popravak", false),
      remove = Ui.button("Ukloni odabranu stavku", false),
      save = Ui.button("Spremi servis", true),
      cancel = Ui.button("Odustani", false);
  private final List<ProblemRow> problems;
  private final boolean[] selected;
  private final boolean historical;

  public ServiceEditorDialog(Window owner, int km, boolean historical, List<ProblemRow> problems) {
    super(
        owner,
        historical ? "Pocetna povijest - novi zapis" : "Novi servis",
        ModalityType.APPLICATION_MODAL);
    this.historical = historical;
    this.problems = List.copyOf(problems);
    selected = new boolean[problems.size()];
    mileage = Ui.mileage(km);
    setSize(940, 700);
    setLocationRelativeTo(owner);
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    JPanel top = Ui.form();
    Ui.field(top, 0, "Datum", date);
    Ui.field(top, 1, "Kilometraza pri servisu", mileage);
    root.add(top, BorderLayout.NORTH);
    JPanel middle = new JPanel(new BorderLayout(8, 8));
    middle.add(Ui.row(addMaintenance, addRepair, remove), BorderLayout.NORTH);
    itemTable.setRowHeight(32);
    itemTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    itemTable.getTableHeader().setReorderingAllowed(false);
    middle.add(new JScrollPane(itemTable));
    JPanel lower = Ui.column();
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    lower.add(new JLabel("Napomena"));
    lower.add(new JScrollPane(note));
    lower.add(
        Ui.hint(
            historical
                ? "Nepoznata stara cijena ostaje prazna; 0 znaci poznat nulti trosak."
                : "Cijena je stvarno placeni ukupan iznos po radu. Npr. 120,50."));
    if (!problems.isEmpty()) {
      JTable p =
          new JTable(
              new AbstractTableModel() {
                public int getRowCount() {
                  return problems.size();
                }

                public int getColumnCount() {
                  return 2;
                }

                public String getColumnName(int c) {
                  return c == 0 ? "Rijesen" : "Problem rijesen ovim servisom";
                }

                public Class<?> getColumnClass(int c) {
                  return c == 0 ? Boolean.class : String.class;
                }

                public Object getValueAt(int r, int c) {
                  return c == 0 ? selected[r] : problems.get(r).getDescription();
                }

                public boolean isCellEditable(int r, int c) {
                  return c == 0;
                }

                public void setValueAt(Object value, int r, int c) {
                  selected[r] = Boolean.TRUE.equals(value);
                  fireTableCellUpdated(r, c);
                }
              });
      p.setRowHeight(28);
      p.getColumnModel().getColumn(0).setMaxWidth(80);
      JScrollPane sc = new JScrollPane(p);
      sc.setPreferredSize(new Dimension(650, 110));
      lower.add(sc);
    }
    middle.add(lower, BorderLayout.SOUTH);
    root.add(middle);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);
    setContentPane(root);
    getRootPane().setDefaultButton(save);
  }

  public ServiceInput input() {
    if (itemTable.isEditing() && !itemTable.getCellEditor().stopCellEditing()) {
      throw new IllegalArgumentException("Potvrdite valjanu cijenu u tablici.");
    }
    List<Long> ids = new ArrayList<>();
    for (int i = 0; i < selected.length; i++) {
      if (selected[i]) {
        ids.add(problems.get(i).getId());
      }
    }
    return new ServiceInput(
        date.date(),
        Ui.integer(mileage),
        note.getText(),
        items.snapshot(historical),
        ids);
  }

}
