package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Upravlja ovisnim izborima u VehicleForm komponenti.
 *
 * <p>Promjena marke ponovno učitava modele, promjena modela godine, a promjena godine dostupne
 * varijante. Time View prikazuje samo kombinacije koje postoje u katalogu vozila.
 */
public class VehicleFormController {
  /** Swing forma čije ovisne odabire ovaj Controller učitava iz kataloga. */
  private final VehicleForm view;

  /** Dohvaća marke, modele, godine i varijante za uzastopne izbore u formi. */
  private final CatalogService catalogService;

  /** Sprječava da programsko punjenje ComboBoxova pokrene dodatne dohvatne listenere. */
  private boolean updating;

  /**
   * Povezuje VehicleForm s CatalogServiceom i registrira listenere ovisnih odabira.
   *
   * @param view forma za izbor vozila
   * @param catalogService servis iz kojeg se dohvaćaju kataloške vrijednosti
   */
  public VehicleFormController(VehicleForm view, CatalogService catalogService) {
    this.view = view;
    this.catalogService = catalogService;

    view.make.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadModels();
        }
      }
    });

    view.model.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadYears();
        }
      }
    });

    view.year.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadVariants();
        }
      }
    });

    view.variant.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          view.showDetails(view.selectedVariant());
        }
      }
    });
  }

  /** Učitava početni popis marki i čisti sve ovisne odabire ispod marke. */
  public void loadMakes() {
    try {
      view.state.setText("Učitavanje marki...");
      List<String> values = catalogService.makes();

      updating = true;
      view.reset();
      view.setMakes(values);
      updating = false;

      if (values.isEmpty()) {
        view.state.setText("Nema kataloga za odabir.");
      } else {
        view.state.setText("Odaberite marku.");
      }
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /** Učitava modele za odabranu marku te resetira godinu i varijantu. */
  private void loadModels() {
    String make = (String) view.make.getSelectedItem();

    updating = true;
    view.clearBelowMake();
    updating = false;

    if (make == null) {
      view.state.setText("Odaberite marku.");
      return;
    }

    try {
      view.state.setText("Učitavanje modela...");
      List<String> values = catalogService.models(make);

      updating = true;
      view.setModels(values);
      updating = false;

      view.state.setText("Odaberite model.");
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /** Učitava dostupne godine za odabranu marku i model te resetira varijantu. */
  private void loadYears() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();

    updating = true;
    view.clearBelowModel();
    updating = false;

    if (make == null || model == null) {
      view.state.setText("Odaberite model.");
      return;
    }

    try {
      view.state.setText("Učitavanje godina...");
      List<Integer> values = catalogService.years(make, model);

      updating = true;
      view.setYears(values);
      updating = false;

      view.state.setText("Odaberite godinu proizvodnje.");
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /**
   * Učitava varijante koje odgovaraju odabranoj marki, modelu i godini te prikazuje detalj
   * trenutno odabrane varijante.
   */
  private void loadVariants() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();
    Integer year = (Integer) view.year.getSelectedItem();

    updating = true;
    view.clearBelowYear();
    updating = false;

    if (make == null || model == null || year == null) {
      view.state.setText("Odaberite godinu proizvodnje.");
      return;
    }

    try {
      view.state.setText("Učitavanje varijanti...");
      List<VehicleVariant> values = catalogService.variants(make, model, year);

      updating = true;
      view.setVariants(values);
      updating = false;

      if (values.isEmpty()) {
        view.state.setText("Nema varijanti za odabranu godinu.");
      } else {
        view.state.setText("Odaberite točnu varijantu.");
      }
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /** Vraća cijelu formu vozila u početno stanje i ponovno učitava marke. */
  public void reset() {
    updating = true;
    view.reset();
    updating = false;
  }
}
