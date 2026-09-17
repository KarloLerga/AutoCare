package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Racun, prvo vozilo i opcionalni stari servisi su draft do zavrsnog gumba. */
public final class OnboardingDialog extends JDialog {
  public final JTextField name = new JTextField(25);
  public final JTextField email = new JTextField(25);
  public final JPasswordField password = new JPasswordField(25);
  public final JPasswordField repeat = new JPasswordField(25);
  public final VehicleForm vehicle = new VehicleForm();
  public final JButton back = Ui.button("Natrag");
  public final JButton next = Ui.button("Nastavi");
  public final JButton finish = Ui.button("Zavrsi registraciju");
  public final JButton cancel = Ui.button("Odustani");
  public final JButton addHistory = Ui.button("Dodaj poznati servis");
  public final JButton removeHistory = Ui.button("Ukloni odabrani servis");
  public final JTable historyTable;
  private final DefaultTableModel historyTableModel;
  private List<ServiceInput> history = new ArrayList<>();
  private final CardLayout cards = new CardLayout();
  private final JPanel body = new JPanel(cards);
  private final JLabel stepLabel = Ui.heading("1 / 3 - Korisnicki racun");
  private int step;

  public OnboardingDialog(Window owner) {
    super(owner, "Registracija - AutoCare", ModalityType.APPLICATION_MODAL);
    setSize(920, 720);
    setLocationRelativeTo(owner);
    historyTableModel =
        new DefaultTableModel(
            new Object[][] {}, new String[] {"Datum", "Km", "Broj stavki", "Napomena"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    historyTable = new JTable(historyTableModel);
    historyTable.setRowHeight(30);
    historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    historyTable.setFillsViewportHeight(true);
    historyTable.getTableHeader().setReorderingAllowed(false);

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(stepLabel, BorderLayout.NORTH);
    JPanel account = Ui.form();
    Ui.field(account, 0, "Ime", name);
    Ui.field(account, 1, "E-mail", email);
    Ui.field(account, 2, "Lozinka (12 - 128 znakova)", password);
    Ui.field(account, 3, "Ponovi lozinku", repeat);
    body.add(account, "0");
    body.add(vehicle, "1");
    JPanel historyPanel = new JPanel(new BorderLayout(8, 8));
    historyPanel.add(Ui.row(addHistory, removeHistory), BorderLayout.NORTH);
    historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
    historyPanel.add(
        Ui.hint("Ovaj korak je opcionalan. Cijena starog servisa smije ostati nepoznata."),
        BorderLayout.SOUTH);
    body.add(historyPanel, "2");
    root.add(body, BorderLayout.CENTER);
    root.add(Ui.row(cancel, back, next, finish), BorderLayout.SOUTH);
    setContentPane(root);
    step(0);
  }

  public int step() {
    return step;
  }

  public void step(int value) {
    step = value;
    cards.show(body, Integer.toString(value));
    String[] labels = {
      "1 / 3 - Korisnicki racun", "2 / 3 - Prvo vozilo", "3 / 3 - Poznata servisna povijest"
    };
    stepLabel.setText(labels[value]);
    back.setEnabled(value > 0);
    next.setVisible(value < 2);
    finish.setVisible(value == 2);
    getRootPane().setDefaultButton(value == 2 ? finish : next);
  }

  public void setHistory(List<ServiceInput> values) {
    history = new ArrayList<>(values);
    historyTableModel.setRowCount(0);
    for (ServiceInput service : history) {
      historyTableModel.addRow(
          new Object[] {
            service.getDate(), service.getMileage(), service.getItems().size(), service.getNote()
          });
    }
  }

  public ServiceInput selectedHistory() {
    int selectedRow = historyTable.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return history.get(historyTable.convertRowIndexToModel(selectedRow));
  }

  public void clearPasswords() {
    password.setText("");
    repeat.setText("");
  }
}
