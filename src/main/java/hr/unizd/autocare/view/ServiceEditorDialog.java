package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.components.ServiceItemsModel;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Forma za unos stvarnog ili povijesnog servisa. */
public final class ServiceEditorDialog extends JDialog {
  public final JTextField date = new JTextField(Ui.date(LocalDate.now()), 12);
  public final JSpinner mileage;
  public final JTextArea note = new JTextArea(3, 25);
  public final ServiceItemsModel items = new ServiceItemsModel();
  public final JTable itemTable = new JTable(items);
  public final JButton addMaintenance = Ui.button("Dodaj odrzavanje", false);
  public final JButton addRepair = Ui.button("Dodaj popravak", false);
  public final JButton remove = Ui.button("Ukloni odabranu stavku", false);
  public final JButton save = Ui.button("Spremi servis", true);
  public final JButton cancel = Ui.button("Odustani", false);

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
    mileage = Ui.mileage(mileageValue);

    problemsTableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Rijesen", "Problem rijesen ovim servisom"}) {
          @Override
          public Class<?> getColumnClass(int column) {
            if (column == 0) {
              return Boolean.class;
            }
            return String.class;
          }

          @Override
          public boolean isCellEditable(int row, int column) {
            return column == 0;
          }
        };

    for (ProblemRow problem : this.problems) {
      problemsTableModel.addRow(new Object[] {Boolean.FALSE, problem.getDescription()});
    }

    setSize(940, 700);
    setLocationRelativeTo(owner);

    date.setToolTipText("Datum u obliku 15.09.2026.");

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
    middle.add(new JScrollPane(itemTable), BorderLayout.CENTER);

    JPanel lower = Ui.column();
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    lower.add(new JLabel("Napomena"));
    lower.add(new JScrollPane(note));

    if (historical) {
      lower.add(Ui.hint("Nepoznata stara cijena moze ostati prazna."));
    } else {
      lower.add(Ui.hint("Upisite stvarno placeni iznos za svaki rad."));
    }

    if (!this.problems.isEmpty()) {
      JTable problemTable = new JTable(problemsTableModel);
      problemTable.setRowHeight(28);
      problemTable.getColumnModel().getColumn(0).setMaxWidth(80);

      JScrollPane problemScrollPane = new JScrollPane(problemTable);
      problemScrollPane.setPreferredSize(new Dimension(650, 110));
      lower.add(problemScrollPane);
    }

    middle.add(lower, BorderLayout.SOUTH);
    root.add(middle, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);

    setContentPane(root);
    getRootPane().setDefaultButton(save);
  }

  public WorkRow chooseWork(List<WorkRow> works) {
    if (works.isEmpty()) {
      Ui.info(this, "Nema dostupnih radova.");
      return null;
    }

    DefaultListModel<String> model = new DefaultListModel<>();
    for (WorkRow work : works) {
      model.addElement(work.getName() + " - " + Ui.estimate(work.getPrice()));
    }

    JList<String> list = new JList<>(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setVisibleRowCount(12);

    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(new Dimension(520, 300));

    int answer =
        JOptionPane.showConfirmDialog(
            this,
            scrollPane,
            "Odaberite rad",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

    if (answer != JOptionPane.OK_OPTION) {
      return null;
    }

    int selectedIndex = list.getSelectedIndex();
    if (selectedIndex < 0) {
      Ui.info(this, "Odaberite rad.");
      return null;
    }

    return works.get(selectedIndex);
  }

  public ServiceInput input() {
    if (itemTable.isEditing() && !itemTable.getCellEditor().stopCellEditing()) {
      throw new IllegalArgumentException("Potvrdite valjanu cijenu u tablici.");
    }

    List<Long> resolvedProblemIds = new ArrayList<>();
    for (int row = 0; row < problemsTableModel.getRowCount(); row++) {
      if (Boolean.TRUE.equals(problemsTableModel.getValueAt(row, 0))) {
        resolvedProblemIds.add(problems.get(row).getId());
      }
    }

    return new ServiceInput(
        Ui.parseDate(date.getText()),
        Ui.integer(mileage),
        note.getText(),
        items.snapshot(historical),
        resolvedProblemIds);
  }
}
