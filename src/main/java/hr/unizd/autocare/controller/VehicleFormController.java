package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public final class VehicleFormController {
  private final VehicleForm view;
  private final CatalogService catalogService;
  private boolean updating;

  public VehicleFormController(VehicleForm view, CatalogService catalogService) {
    this.view = view;
    this.catalogService = catalogService;

    view.make.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!updating) {
              loadModels();
            }
          }
        });

    view.model.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!updating) {
              loadYears();
            }
          }
        });

    view.year.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!updating) {
              loadVariants();
            }
          }
        });

    view.variant.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (!updating) {
              view.showDetails(view.selectedVariant());
            }
          }
        });
  }

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

  public void reset() {
    updating = true;
    view.reset();
    updating = false;
  }
}
