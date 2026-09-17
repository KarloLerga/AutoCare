package hr.unizd.autocare.view.components;

import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Picker identiteta; nema SQL-a. Godina, marka, model i filtrirane varijante. */
public final class VehicleForm extends JPanel {
  public final JSpinner year =
      new JSpinner(
          new SpinnerNumberModel(LocalDate.now().getYear(), 1886, LocalDate.now().getYear(), 1));
  public final JComboBox<String> make = new JComboBox<>();
  public final JComboBox<String> model = new JComboBox<>();
  public final JTextField search = new JTextField(18);
  public final JButton find = Ui.button("Pretrazi varijante", false);
  public final JSpinner mileage = Ui.mileage(0);
  public final JLabel state = Ui.hint("Odaberite godinu, marku, model i tocnu varijantu.");
  public final JTable variants;
  public boolean updating;
  private final DefaultTableModel variantsModel;
  private List<VariantRow> variantRows = new ArrayList<>();

  public VehicleForm() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    variantsModel =
        new DefaultTableModel(
            new Object[][] {},
            new String[] {"Generacija", "Motor", "Gorivo", "Mjenjac", "KS", "Od", "Do"}) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    variants = new JTable(variantsModel);
    variants.setRowHeight(30);
    variants.setAutoCreateRowSorter(true);
    variants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    variants.setFillsViewportHeight(true);
    variants.getTableHeader().setReorderingAllowed(false);
    year.setEditor(new JSpinner.NumberEditor(year, "0"));
    JPanel form = Ui.form();
    Ui.field(form, 0, "Godina proizvodnje", year);
    Ui.field(form, 1, "Marka", make);
    Ui.field(form, 2, "Model", model);
    Ui.field(form, 3, "Motor ili generacija", Ui.row(search, find));
    Ui.field(form, 4, "Trenutna kilometraza", mileage);
    add(form, BorderLayout.NORTH);
    setPreferredSize(new Dimension(700, 420));
    add(new JScrollPane(variants), BorderLayout.CENTER);
    add(state, BorderLayout.SOUTH);
    make.setMaximumRowCount(18);
    model.setMaximumRowCount(18);
  }

  public VehicleInput input() {
    VariantRow selectedVariant = selectedVariant();
    if (selectedVariant == null) {
      throw new IllegalArgumentException("Odaberite tocnu varijantu u tablici.");
    }
    return new VehicleInput(selectedVariant.getId(), Ui.integer(year), Ui.integer(mileage));
  }

  public void setVariants(List<VariantRow> values) {
    variantRows = new ArrayList<>(values);
    variantsModel.setRowCount(0);
    for (VariantRow variant : variantRows) {
      variantsModel.addRow(
          new Object[] {
            variant.getGeneration(),
            variant.getEngine(),
            variant.getFuel(),
            variant.getTransmission(),
            variant.getPowerHp(),
            variant.getFrom(),
            variant.getTo()
          });
    }
  }

  public List<VariantRow> variantRows() {
    return variantRows;
  }

  public VariantRow selectedVariant() {
    int selectedRow = variants.getSelectedRow();
    if (selectedRow < 0) {
      return null;
    }
    return variantRows.get(variants.convertRowIndexToModel(selectedRow));
  }

  public void existing(VehicleRow vehicle, boolean identityEditable) {
    updating = true;
    year.setValue(vehicle.getYear());
    make.setModel(new DefaultComboBoxModel<>(new String[] {vehicle.getVariant().getMake()}));
    model.setModel(new DefaultComboBoxModel<>(new String[] {vehicle.getVariant().getModel()}));
    List<VariantRow> values = new ArrayList<>();
    values.add(vehicle.getVariant());
    setVariants(values);
    variants.setRowSelectionInterval(0, 0);
    mileage.setValue(vehicle.getMileage());
    year.setEnabled(identityEditable);
    make.setEnabled(identityEditable);
    model.setEnabled(identityEditable);
    search.setEnabled(identityEditable);
    find.setEnabled(identityEditable);
    if (identityEditable) {
      state.setText("Za promjenu identiteta prvo ponovno odaberite godinu i katalog.");
    } else {
      state.setText(
          "Identitet je zakljucan jer vozilo ima povijest; kilometraza se moze povecati.");
    }
    updating = false;
  }
}
