package hr.unizd.autocare.view.components;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.VehicleInput;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class VehicleForm extends JPanel {
  public final JComboBox<String> make = new JComboBox<>();
  public final JComboBox<String> model = new JComboBox<>();
  public final JComboBox<Integer> year = new JComboBox<>();
  public final JComboBox<VehicleVariant> variant = new JComboBox<>();
  public final JTextField mileage = new JTextField(12);
  public final JLabel details = Ui.hint("Odaberite točnu varijantu.");
  public final JLabel state = Ui.hint("Odaberite marku, model, godinu i varijantu.");

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
    VehicleVariant selected = selectedVariant();
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
    make.removeAllItems();
    for (String value : values) {
      make.addItem(value);
    }
    make.setSelectedIndex(-1);
  }

  public void setModels(List<String> values) {
    model.removeAllItems();
    for (String value : values) {
      model.addItem(value);
    }
    model.setSelectedIndex(-1);
  }

  public void setYears(List<Integer> values) {
    year.removeAllItems();
    for (Integer value : values) {
      year.addItem(value);
    }
    year.setSelectedIndex(-1);
  }

  public void setVariants(List<VehicleVariant> values) {
    variant.removeAllItems();
    for (VehicleVariant value : values) {
      variant.addItem(value);
    }
    variant.setSelectedIndex(-1);
    details.setText("Odaberite točnu varijantu.");
  }

  public VehicleVariant selectedVariant() {
    return (VehicleVariant) variant.getSelectedItem();
  }

  public void clearBelowMake() {
    model.removeAllItems();
    year.removeAllItems();
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  public void clearBelowModel() {
    year.removeAllItems();
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  public void clearBelowYear() {
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  public void reset() {
    make.removeAllItems();
    model.removeAllItems();
    year.removeAllItems();
    variant.removeAllItems();
    mileage.setText("");
    details.setText("Odaberite točnu varijantu.");
    state.setText("Odaberite marku, model, godinu i varijantu.");
  }

  public void showDetails(VehicleVariant selected) {
    if (selected == null) {
      details.setText("Odaberite točnu varijantu.");
      return;
    }

    String power = "? KS";
    if (selected.getPowerHp() != null) {
      power = selected.getPowerHp() + " KS";
    }

    String transmission = selected.getTransmission();
    if (transmission == null || transmission.isBlank()) {
      transmission = "Mjenjač nije naveden";
    }

    details.setText(
        selected.getGeneration()
            + " / "
            + selected.getEngineLabel()
            + " / "
            + selected.getFuelType()
            + " / "
            + power
            + " / "
            + transmission);
  }
}
