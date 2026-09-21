package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Unos stvarno plaćenih stavki servisa bez prikaza procijenjenih cijena. */
public final class ServiceEditorDialog extends JDialog {
  public final JTextField date = new JTextField(Ui.date(LocalDate.now()), 12);
  public final JTextField mileage;
  public final JTextArea note = new JTextArea(3, 25);
  public final JComboBox<String> type = new JComboBox<>(new String[] {"Održavanje", "Popravak"});
  public final JComboBox<WorkDefinition> work = new JComboBox<>();
  public final JTextField actualPrice = new JTextField(12);
  public final JButton addItem = Ui.button("Dodaj stavku");
  public final JButton remove = Ui.button("Ukloni odabranu stavku");
  public final JButton save = Ui.button("Spremi servis");
  public final JButton cancel = Ui.button("Odustani");
  public final JTable itemTable;

  private final List<Problem> problems;
  private final DefaultTableModel itemTableModel;
  private final DefaultTableModel problemsTableModel;
  private final List<AddedItem> items = new ArrayList<>();
  private final List<WorkDefinition> availableWorks = new ArrayList<>();
  private final boolean historical;

  public ServiceEditorDialog(
      Window owner, int mileageValue, boolean historical, List<Problem> problems) {
    super(owner, dialogTitle(historical), ModalityType.APPLICATION_MODAL);
    this.historical = historical;
    this.problems = new ArrayList<>(problems);
    mileage = new JTextField(Integer.toString(mileageValue), 12);

    itemTableModel =
        new DefaultTableModel(new Object[][] {}, new String[] {"Rad", "Stvarno plaćeno"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    itemTable = new JTable(itemTableModel);
    itemTable.setRowHeight(32);
    itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    itemTable.setFillsViewportHeight(true);
    itemTable.getTableHeader().setReorderingAllowed(false);

    problemsTableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Riješen", "Problem riješen ovim servisom"}) {
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
    for (Problem problem : this.problems) {
      problemsTableModel.addRow(new Object[] {Boolean.FALSE, problem.getDescription()});
    }

    setSize(940, 700);
    setLocationRelativeTo(owner);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    date.setToolTipText("Datum u obliku 15.09.2026.");

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
    JPanel top = Ui.form();
    Ui.field(top, 0, "Datum", date);
    Ui.field(top, 1, "Kilometraža pri servisu", mileage);
    root.add(top, BorderLayout.NORTH);

    JPanel middle = new JPanel(new BorderLayout(8, 8));
    JPanel picker = Ui.column();
    picker.add(
        Ui.row(
            new JLabel("Vrsta:"),
            type,
            new JLabel("Rad:"),
            work,
            new JLabel("Stvarno plaćeno:"),
            actualPrice));
    picker.add(Ui.row(addItem, remove));
    middle.add(picker, BorderLayout.NORTH);
    middle.add(new JScrollPane(itemTable), BorderLayout.CENTER);

    JPanel lower = Ui.column();
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    lower.add(new JLabel("Napomena"));
    lower.add(new JScrollPane(note));
    String priceHint = "Unesite stvarno plaćeni iznos za svaku stavku.";
    if (historical) {
      priceHint = "Nepoznatu cijenu starog servisa možete ostaviti praznom.";
    }
    lower.add(Ui.hint(priceHint));
    if (!this.problems.isEmpty()) {
      JTable problemTable = new JTable(problemsTableModel);
      problemTable.setRowHeight(28);
      problemTable.getColumnModel().getColumn(0).setMaxWidth(80);
      JScrollPane problemScroll = new JScrollPane(problemTable);
      problemScroll.setPreferredSize(new Dimension(650, 110));
      lower.add(problemScroll);
    }
    middle.add(lower, BorderLayout.SOUTH);
    root.add(middle, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);
    setContentPane(root);
    getRootPane().setDefaultButton(save);
  }

  private static String dialogTitle(boolean historical) {
    if (historical) {
      return "Početna povijest — novi zapis";
    }
    return "Novi servis";
  }

  public void setWorks(List<WorkDefinition> works) {
    availableWorks.clear();
    availableWorks.addAll(works);
    type.setSelectedIndex(0);
    filterWorks();
  }

  public void filterWorks() {
    WorkCategory selectedCategory = WorkCategory.REPAIR;
    if (type.getSelectedIndex() == 0) {
      selectedCategory = WorkCategory.MAINTENANCE;
    }

    work.removeAllItems();
    for (WorkDefinition workDefinition : availableWorks) {
      if (workDefinition.getCategory() == selectedCategory) {
        work.addItem(workDefinition);
      }
    }

    if (work.getItemCount() > 0) {
      work.setSelectedIndex(0);
    }
  }

  public WorkDefinition selectedWork() {
    return (WorkDefinition) work.getSelectedItem();
  }

  public void addWork(WorkDefinition selected) {
    for (AddedItem item : items) {
      if (item.work.getId().equals(selected.getId())) {
        throw new IllegalArgumentException("Rad je već dodan u servis.");
      }
    }

    items.add(new AddedItem(selected, Ui.parseMoney(actualPrice.getText(), historical)));
    actualPrice.setText("");
    if (work.getItemCount() > 0) {
      work.setSelectedIndex(0);
    }
    refreshItems();
  }

  public void removeSelectedItem() {
    int row = itemTable.getSelectedRow();
    if (row >= 0) {
      items.remove(row);
      refreshItems();
    }
  }

  private void refreshItems() {
    itemTableModel.setRowCount(0);
    for (AddedItem item : items) {
      itemTableModel.addRow(new Object[] {item.work.getName(), Ui.money(item.price)});
    }
  }

  public ServiceInput input() {
    List<Long> resolvedProblemIds = new ArrayList<>();
    for (int row = 0; row < problemsTableModel.getRowCount(); row++) {
      if (Boolean.TRUE.equals(problemsTableModel.getValueAt(row, 0))) {
        resolvedProblemIds.add(problems.get(row).getId());
      }
    }

    List<ItemInput> inputs = new ArrayList<>();
    for (AddedItem item : items) {
      inputs.add(new ItemInput(item.work.getId(), item.price));
    }

    return new ServiceInput(
        Ui.parseDate(date.getText()), Ui.mileage(mileage), note.getText(), inputs, resolvedProblemIds);
  }

  private static final class AddedItem {
    private final WorkDefinition work;
    private final BigDecimal price;

    private AddedItem(WorkDefinition work, BigDecimal price) {
      this.work = work;
      this.price = price;
    }
  }
}
