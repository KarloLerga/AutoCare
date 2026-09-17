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
import javax.swing.table.DefaultTableModel;

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
  private final DefaultTableModel problemsTableModel;
  private final boolean historical;

  public ServiceEditorDialog(
      Window owner, int mileageValue, boolean historical, List<ProblemRow> problems) {
    super(
        owner,
        historical ? "Pocetna povijest - novi zapis" : "Novi servis",
        ModalityType.APPLICATION_MODAL);
    this.historical = historical;
    this.problems = new ArrayList<>(problems);
    problemsTableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Rijesen", "Problem rijesen ovim servisom"}) {
          @Override
          public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
          }

          @Override
          public boolean isCellEditable(int row, int column) {
            return column == 0;
          }
        };
    for (ProblemRow problem : this.problems) {
      problemsTableModel.addRow(new Object[] {Boolean.FALSE, problem.getDescription()});
    }
    mileage = Ui.mileage(mileageValue);
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
    if (!this.problems.isEmpty()) {
      JTable problemTable = new JTable(problemsTableModel);
      problemTable.setRowHeight(28);
      problemTable.getColumnModel().getColumn(0).setMaxWidth(80);
      JScrollPane problemScrollPane = new JScrollPane(problemTable);
      problemScrollPane.setPreferredSize(new Dimension(650, 110));
      lower.add(problemScrollPane);
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
    for (int row = 0; row < problemsTableModel.getRowCount(); row++) {
      if (Boolean.TRUE.equals(problemsTableModel.getValueAt(row, 0))) {
        ids.add(problems.get(row).getId());
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
