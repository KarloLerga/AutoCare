package hr.unizd.autocare.controller;

import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/** Kaskadni picker koji pri svakoj promjeni roditelja poništava stare odabire. */
public final class VehicleFormController {
  private final VehicleForm view;
  private final CatalogService catalogService;

  public VehicleFormController(VehicleForm view, CatalogService catalogService) {
    this.view = view;
    this.catalogService = catalogService;
    view.make.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!view.updating) {
              loadModels();
            }
          }
        });
    view.model.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!view.updating) {
              loadYears();
            }
          }
        });
    view.year.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!view.updating) {
              loadVariants();
            }
          }
        });
    view.variant.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!view.updating) {
              view.showDetails(view.selectedVariant());
            }
          }
        });
  }

  public void loadMakes() {
    try {
      view.state.setText("Učitavanje marki...");
      view.updating = true;
      view.clearBelowMake();
      view.updating = false;
      List<String> values = catalogService.makes();
      view.updating = true;
      view.setMakes(values);
      view.updating = false;
      view.state.setText(values.isEmpty() ? "Nema kataloga za odabir." : "Odaberite marku.");
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }

  private void loadModels() {
    String make = (String) view.make.getSelectedItem();
    view.clearBelowMake();
    if (make == null) {
      view.state.setText("Odaberite marku.");
      return;
    }
    try {
      view.state.setText("Učitavanje modela...");
      view.updating = true;
      view.setModels(catalogService.models(make));
      view.updating = false;
      view.state.setText("Odaberite model.");
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }

  private void loadYears() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();
    view.clearBelowModel();
    if (make == null || model == null) {
      view.state.setText("Odaberite model.");
      return;
    }
    try {
      view.state.setText("Učitavanje godina...");
      view.updating = true;
      view.setYears(catalogService.years(make, model));
      view.updating = false;
      view.state.setText("Odaberite godinu proizvodnje.");
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }

  private void loadVariants() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();
    Integer year = (Integer) view.year.getSelectedItem();
    view.clearBelowYear();
    if (make == null || model == null || year == null) {
      view.state.setText("Odaberite godinu proizvodnje.");
      return;
    }
    try {
      view.state.setText("Učitavanje varijanti...");
      List<VariantRow> values = catalogService.variants(make, model, year);
      view.updating = true;
      view.setVariants(values);
      view.updating = false;
      if (values.isEmpty()) {
        view.state.setText("Nema varijanti za odabranu godinu.");
      } else {
        view.state.setText("Odaberite točnu varijantu.");
      }
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }
}
