package hr.unizd.autocare.view.components;

import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.VehicleInput;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Kaskadni izbor marka → model → godina → točna varijanta. */
public final class VehicleForm extends JPanel {
  public final JComboBox<String> make = new JComboBox<>();
  public final JComboBox<String> model = new JComboBox<>();
  public final JComboBox<Integer> year = new JComboBox<>();
  public final JComboBox<VariantRow> variant = new JComboBox<>();
  public final JTextField mileage = new JTextField("0", 12);
  public final JLabel details = Ui.hint("Odaberite točnu varijantu.");
  public final JLabel state = Ui.hint("Odaberite marku, model, godinu i varijantu.");
  public boolean updating;

  private List<VariantRow> variantRows = new ArrayList<>();

  public VehicleForm() {
    super(new BorderLayout(12, 12));
    setOpaque(false);

    JPanel form = Ui.form();
    Ui.field(form, 0, "Marka", make);
    Ui.field(form, 1, "Model", model);
    Ui.field(form, 2, "Godina proizvodnje", year);
    Ui.field(form, 3, "Varijanta", variant);
    Ui.field(form, 4, "Trenutna kilometraža", mileage);
    JPanel information = Ui.column();
    information.add(details);
    information.add(state);
    add(form, BorderLayout.NORTH);
    add(information, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(700, 250));
    make.setMaximumRowCount(18);
    model.setMaximumRowCount(18);
    year.setMaximumRowCount(18);
    variant.setMaximumRowCount(18);
  }

  public VehicleInput input() {
    VariantRow selected = selectedVariant();
    if (selected == null) {
      throw new IllegalArgumentException("Odaberite točnu varijantu vozila.");
    }
    Integer selectedYear = (Integer) year.getSelectedItem();
    if (selectedYear == null) {
      throw new IllegalArgumentException("Odaberite godinu proizvodnje.");
    }
    return new VehicleInput(selected.getId(), selectedYear, Ui.mileage(mileage));
  }

  public void setMakes(List<String> values) {
    make.setModel(new DefaultComboBoxModel<>(values.toArray(new String[0])));
    make.setSelectedIndex(-1);
  }

  public void setModels(List<String> values) {
    model.setModel(new DefaultComboBoxModel<>(values.toArray(new String[0])));
    model.setSelectedIndex(-1);
  }

  public void setYears(List<Integer> values) {
    year.setModel(new DefaultComboBoxModel<>(values.toArray(new Integer[0])));
    year.setSelectedIndex(-1);
  }

  public void setVariants(List<VariantRow> values) {
    variantRows = new ArrayList<>(values);
    variant.setModel(new DefaultComboBoxModel<>(variantRows.toArray(new VariantRow[0])));
    variant.setSelectedIndex(-1);
    details.setText("Odaberite točnu varijantu.");
  }

  public VariantRow selectedVariant() {
    return (VariantRow) variant.getSelectedItem();
  }

  public List<VariantRow> variants() {
    return variantRows;
  }

  public void clearBelowMake() {
    updating = true;
    setModels(List.of());
    setYears(List.of());
    setVariants(List.of());
    updating = false;
  }

  public void clearBelowModel() {
    updating = true;
    setYears(List.of());
    setVariants(List.of());
    updating = false;
  }

  public void clearBelowYear() {
    updating = true;
    setVariants(List.of());
    updating = false;
  }

  public void showDetails(VariantRow selected) {
    if (selected == null) {
      details.setText("Odaberite točnu varijantu.");
      return;
    }
    String power = selected.getPowerHp() == null ? "? KS" : selected.getPowerHp() + " KS";
    details.setText(
        selected.getGeneration()
            + " / "
            + selected.getEngine()
            + " / "
            + selected.getFuel()
            + " / "
            + power
            + " / "
            + selected.getTransmission());
  }
}
