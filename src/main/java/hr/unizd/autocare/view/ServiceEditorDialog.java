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

/**
 * Modalni editor za unos novog stvarnog servisa vozila.
 *
 * <p>Korisnik bira datum, kilometražu, radove i stvarne cijene te može označiti otvorene probleme
 * koje je taj servis riješio. Dijalog ne sprema podatke izravno u bazu nego ih kroz
 * {@link ServiceInput} predaje Controlleru.
 */
public class ServiceEditorDialog extends JDialog {
  /** Datum stvarno obavljenog servisa. */
  public final JTextField date = new JTextField(Ui.date(LocalDate.now()), 12);

  /** Kilometraža vozila pri servisu, početno postavljena trenutačnom kilometražom. */
  public final JTextField mileage;

  /** Neobavezna napomena koja se sprema uz servis. */
  public final JTextArea note = new JTextArea(3, 25);

  /** Filtar radova prema tome radi li se o održavanju ili popravku. */
  public final JComboBox<String> type = new JComboBox<>(new String[] {"Održavanje", "Popravak"});

  /** Odabir standardnog rada koji se dodaje kao servisna stavka. */
  public final JComboBox<WorkDefinition> work = new JComboBox<>();

  /** Stvarna cijena odabranog rada, ne informativna kataloška cijena. */
  public final JTextField actualPrice = new JTextField(12);

  /** Dodaje odabrani rad i cijenu u privremeni popis stavki. */
  public final JButton addItem = new JButton("Dodaj stavku");

  /** Uklanja označenu stavku iz privremenog popisa. */
  public final JButton remove = new JButton("Ukloni odabranu stavku");

  /** Predaje sastavljeni unos Controlleru na spremanje. */
  public final JButton save = new JButton("Spremi servis");

  /** Zatvara editor bez spremanja unosa. */
  public final JButton cancel = new JButton("Odustani");

  /** Tablica privremeno dodanih radova i njihovih stvarnih cijena. */
  public final JTable itemTable;

  /** Otvoreni problemi prikazani kao izbori za povezivanje s novim servisom. */
  private final List<Problem> problems;

  /** Model stavki servisa koji puni {@link #refreshItems()}. */
  private final DefaultTableModel itemTableModel;

  /** Model problema čiji prvi stupac omogućuje korisniku označiti riješene probleme. */
  private final DefaultTableModel problemsTableModel;

  /** Privremeni radovi i cijene prije nego ih {@link #input()} pretvori u ServiceInput. */
  private final List<AddedItem> items = new ArrayList<>();

  /** Katalog radova koji se filtrira prema odabranoj vrsti. */
  private final List<WorkDefinition> availableWorks = new ArrayList<>();

  /**
   * Stvara editor servisa s početnom kilometražom i popisom otvorenih problema.
   *
   * @param owner roditeljski prozor
   * @param mileageValue trenutačna kilometraža koja se nudi kao početna vrijednost
   * @param problems otvoreni problemi koje je moguće označiti kao riješene
   */
  public ServiceEditorDialog(Window owner, int mileageValue, List<Problem> problems) {
    super(owner, "Novi servis", ModalityType.APPLICATION_MODAL);
    this.problems = new ArrayList<>(problems);
    mileage = new JTextField(Integer.toString(mileageValue), 12);

    itemTableModel =
        new DefaultTableModel(new Object[][] {}, new String[] {"Rad", "Stvarno plaćeno"}) {
          /** Privremene stavke ostaju samo za čitanje; dodaju se i uklanjaju gumbima dijaloga. */
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
          /** Prvu kolonu problema određuje kao potvrdni okvir, a drugu kao običan tekst. */
          @Override
          public Class<?> getColumnClass(int column) {
            if (column == 0) {
              return Boolean.class;
            }
            return String.class;
          }

          /** Dopušta označiti problem riješenim, ali ne i uređivati njegov opis. */
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
    lower.add(Ui.hint("Unesite stvarno plaćeni iznos za svaku stavku."));
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


  /**
   * Kopira radove koji se mogu dodati i postavlja početni prikaz na održavanje.
   *
   * <p>Popis se kopira u interni izvorni skup kako filtriranje ne bi mijenjalo kolekciju pozivatelja.
   * Zatim se odabire prva vrsta rada i {@link #filterWorks()} popunjava odgovarajući ComboBox.
   *
   * @param works dostupne definicije radova
   */
  public void setWorks(List<WorkDefinition> works) {
    availableWorks.clear();
    availableWorks.addAll(works);
    type.setSelectedIndex(0);
    filterWorks();
  }

  /**
   * Prikazuje samo radove koji odgovaraju trenutačno odabranoj vrsti.
   *
   * <p>Indeks nula predstavlja održavanje, a druga ponuđena vrsta popravak. Metoda očisti postojeće
   * opcije, doda podudarne radove iz {@code availableWorks} i odabere prvi ako postoji; ne mijenja
   * izvorni popis radova.
   */
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

  /** Dohvaća kataloški rad koji će se dodati u privremeni popis servisnih stavki.
   *
   * @return trenutno odabrani standardni rad ili {@code null} ako odabir ne postoji
   */
  public WorkDefinition selectedWork() {
    return (WorkDefinition) work.getSelectedItem();
  }

  /**
   * Dodaje odabrani rad u privremeni popis stavki servisa sa stvarno plaćenom cijenom iz forme.
   *
   * <p>Prije dodavanja provjerava da isti kataloški rad već nije unesen. Cijenu parsira kao novčani
   * iznos, sprema je uz rad u internom popisu, čisti polje cijene, vraća izbor rada na početak i
   * osvježava tablicu.
   *
   * @param selected rad koji se dodaje
   * @throws IllegalArgumentException ako je isti rad već dodan ili cijena nije valjana
   */
  public void addWork(WorkDefinition selected) {
    for (AddedItem item : items) {
      if (item.work.getId().equals(selected.getId())) {
        throw new IllegalArgumentException("Rad je već dodan u servis.");
      }
    }

    items.add(new AddedItem(selected, Ui.parseMoney(actualPrice.getText())));
    actualPrice.setText("");
    if (work.getItemCount() > 0) {
      work.setSelectedIndex(0);
    }
    refreshItems();
  }

  /** Uklanja trenutno označenu privremenu servisnu stavku iz editora; bez odabira ne mijenja popis. */
  public void removeSelectedItem() {
    int row = itemTable.getSelectedRow();
    if (row >= 0) {
      items.remove(row);
      refreshItems();
    }
  }

  /** Ponovno gradi retke tablice servisnih stavki iz internog popisa {@code AddedItem} objekata. */
  private void refreshItems() {
    itemTableModel.setRowCount(0);
    for (AddedItem item : items) {
      itemTableModel.addRow(new Object[] {item.work.getName(), Ui.money(item.price)});
    }
  }

  /**
   * Pretvara trenutačno stanje forme u ServiceInput za ServiceRecordService.
   *
   * <p>Iz označenih redaka tablice problema prikuplja ID-eve za rješavanje, a privremene radove
   * pretvara u {@link ItemInput} vrijednosti. Datum i kilometražu parsira iz polja forme, napomenu
   * preuzima kao tekst te sve vrijednosti kopira u {@link ServiceInput}; ova metoda ne pristupa
   * bazi.
   *
   * @return neovisni ulazni objekt novog servisa
   * @throws IllegalArgumentException ako datum, kilometraža ili neka druga parsirana vrijednost nije valjana
   */
  public ServiceInput input() {
    List<Integer> resolvedProblemIds = new ArrayList<>();
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

  /** Privremeno povezuje odabrani rad i stvarno plaćenu cijenu prije spremanja servisa. */
  private static final class AddedItem {
    /** Kataloški rad odabran za trenutačni servis. */
    private final WorkDefinition work;

    /** Stvarna cijena unesena za izvedbu rada. */
    private final BigDecimal price;

    /**
     * Čuva jedan odabrani rad i njegovu stvarnu cijenu dok se servis ne pretvori u ulazni model.
     *
     * @param work odabrani standardni rad
     * @param price stvarno plaćena cijena unesena u editoru
     */
    private AddedItem(WorkDefinition work, BigDecimal price) {
      this.work = work;
      this.price = price;
    }
  }
}
