package hr.unizd.autocare.view.components;

import hr.unizd.autocare.domain.VehicleVariant;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Ponovno upotrebljiva Swing forma za izbor marke, modela, godine i kataloške varijante vozila.
 *
 * <p>Forma sama ne dohvaća podatke iz baze; VehicleFormController puni njezine combo boxove i
 * reagira na promjene odabira.
 */
public class VehicleForm extends JPanel {
  /** Marka vozila; izborom se pokreće dohvat dostupnih modela. */
  public final JComboBox<String> make = new JComboBox<>();

  /** Model odabrane marke; izborom se pokreće dohvat dostupnih godina. */
  public final JComboBox<String> model = new JComboBox<>();

  /** Godina proizvodnje; izborom se pokreće dohvat kataloških varijanti. */
  public final JComboBox<Integer> year = new JComboBox<>();

  /** Točna kataloška varijanta vozila unutar izabrane marke, modela i godine. */
  public final JComboBox<VehicleVariant> variant = new JComboBox<>();

  /** Trenutačna kilometraža konkretnog primjerka koju korisnik unosi. */
  public final JTextField mileage = new JTextField(12);

  /** Kratki tehnički opis trenutačno odabrane varijante. */
  public final JLabel details = Ui.hint("Odaberite točnu varijantu.");

  /** Uputa ili status kojim Controller opisuje trenutačni korak odabira. */
  public final JLabel state = Ui.hint("Odaberite marku, model, godinu i varijantu.");

  /** Stvara sva polja forme, detalj odabrane varijante i početne pomoćne poruke. */
  public VehicleForm() {
    super(new BorderLayout(12, 12));
    setOpaque(false);

    JPanel form = Ui.form();
    Ui.field(form, 0, "Marka", make);
    Ui.field(form, 1, "Model", model);
    Ui.field(form, 2, "Godina proizvodnje", year);
    Ui.field(form, 3, "Varijanta", variant);
    Ui.field(form, 4, "Trenutna kilometraža", mileage);

    add(form, BorderLayout.NORTH);
    setPreferredSize(new Dimension(700, 205));

    make.setMaximumRowCount(18);
    model.setMaximumRowCount(18);
    year.setMaximumRowCount(18);
    variant.setMaximumRowCount(18);
  }

  /**
   * Vraća odabranu godinu proizvodnje.
   *
   * @return odabrana godina
   * @throws IllegalArgumentException ako godina nije odabrana
   */
  public int getSelectedYear() {
    Integer selectedYear = (Integer) year.getSelectedItem();
    if (selectedYear == null) {
      throw new IllegalArgumentException("Odaberite godinu proizvodnje.");
    }
    return selectedYear;
  }

  /**
   * Parsira i validira kilometražu unesenu u formu.
   *
   * @return valjana kilometraža
   * @throws IllegalArgumentException ako kilometraža nije valjan nenegativan cijeli broj
   */
  public int getMileage() {
    return Ui.mileage(mileage);
  }

  /**
   * Zamjenjuje vrijednosti odabira marke novim vrijednostima iz kataloga.
   *
   * @param values marke koje treba prikazati
   */
  public void setMakes(List<String> values) {
    make.removeAllItems();
    for (String value : values) {
      make.addItem(value);
    }
    make.setSelectedIndex(-1);
  }

  /**
   * Zamjenjuje vrijednosti odabira modela novim vrijednostima iz kataloga.
   *
   * @param values modeli koje treba prikazati
   */
  public void setModels(List<String> values) {
    model.removeAllItems();
    for (String value : values) {
      model.addItem(value);
    }
    model.setSelectedIndex(-1);
  }

  /**
   * Zamjenjuje vrijednosti odabira godine novim vrijednostima iz kataloga.
   *
   * @param values godine koje treba prikazati
   */
  public void setYears(List<Integer> values) {
    year.removeAllItems();
    for (Integer value : values) {
      year.addItem(value);
    }
    year.setSelectedIndex(-1);
  }

  /**
   * Zamjenjuje vrijednosti odabira varijante novim vrijednostima iz kataloga.
   *
   * @param values varijante koje treba prikazati
   */
  public void setVariants(List<VehicleVariant> values) {
    variant.removeAllItems();
    for (VehicleVariant value : values) {
      variant.addItem(value);
    }
    variant.setSelectedIndex(-1);
    details.setText("Odaberite točnu varijantu.");
  }

  /** Dohvaća preciznu varijantu odabranu nakon izbora marke, modela i godine.
   *
   * @return trenutno odabrana kataloška varijanta ili {@code null} ako nije odabrana
   */
  public VehicleVariant selectedVariant() {
    return (VehicleVariant) variant.getSelectedItem();
  }

  /** Čisti model, godinu i varijantu nakon promjene ili resetiranja marke. */
  public void clearBelowMake() {
    model.removeAllItems();
    year.removeAllItems();
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  /** Čisti godinu i varijantu nakon promjene ili resetiranja modela. */
  public void clearBelowModel() {
    year.removeAllItems();
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  /** Čisti varijantu nakon promjene ili resetiranja godine. */
  public void clearBelowYear() {
    variant.removeAllItems();
    details.setText("Odaberite točnu varijantu.");
  }

  /** Vraća odabire, kilometražu i pomoćni tekst forme u početno stanje. */
  public void reset() {
    make.removeAllItems();
    model.removeAllItems();
    year.removeAllItems();
    variant.removeAllItems();
    mileage.setText("");
    details.setText("Odaberite točnu varijantu.");
    state.setText("Odaberite marku, model, godinu i varijantu.");
  }

  /**
   * Prikazuje sažetak motora, goriva, snage i mjenjača odabrane varijante.
   *
   * @param selected varijanta čije detalje treba prikazati ili {@code null}
   */
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
