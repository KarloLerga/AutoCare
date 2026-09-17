package hr.unizd.autocare.controller;

import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Kaskadni katalog. Svaka promjena roditelja odmah uklanja prethodno odabrani ID. */
public final class VehicleFormController {
  private final VehicleForm view;
  private final CatalogService catalogService;

  public VehicleFormController(VehicleForm view, CatalogService catalogService) {
    this.view = view;
    this.catalogService = catalogService;
    view.year.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent event) {
            if (!view.updating) {
              loadMakes();
            }
          }
        });
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
              loadVariants();
            }
          }
        });
    view.find.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            loadVariants();
          }
        });
    view.search.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            loadVariants();
          }
        });
  }

  public void loadMakes() {
    try {
      int year = Ui.integer(view.year);
      view.updating = true;
      view.make.removeAllItems();
      view.model.removeAllItems();
      view.setVariants(new ArrayList<>());
      view.updating = false;
      view.state.setText("Ucitavanje marki...");
      List<String> values = catalogService.makes(year);
      view.updating = true;
      view.make.setModel(new DefaultComboBoxModel<>(values.toArray(new String[0])));
      view.make.setSelectedIndex(-1);
      view.updating = false;
      if (values.isEmpty()) {
        view.state.setText("Nema kataloga za ovu godinu. Provjerite seed.");
      } else {
        view.state.setText("Odaberite marku.");
      }
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }

  private void loadModels() {
    try {
      String make = (String) view.make.getSelectedItem();
      view.updating = true;
      view.model.removeAllItems();
      view.setVariants(new ArrayList<>());
      view.updating = false;
      if (make == null) {
        return;
      }
      int year = Ui.integer(view.year);
      List<String> values = catalogService.models(year, make);
      view.updating = true;
      view.model.setModel(new DefaultComboBoxModel<>(values.toArray(new String[0])));
      view.model.setSelectedIndex(-1);
      view.updating = false;
      view.state.setText("Odaberite model.");
    } catch (RuntimeException exception) {
      view.updating = false;
      Ui.error(view, exception);
    }
  }

  private void loadVariants() {
    try {
      String make = (String) view.make.getSelectedItem();
      String model = (String) view.model.getSelectedItem();
      String searchText = view.search.getText();
      view.setVariants(new ArrayList<>());
      if (make == null || model == null) {
        return;
      }
      int year = Ui.integer(view.year);
      view.state.setText("Ucitavanje varijanti...");
      List<VariantRow> values = catalogService.variants(year, make, model, searchText);
      boolean more = values.size() > 200;
      List<VariantRow> visibleValues = more ? values.subList(0, 200) : values;
      view.setVariants(visibleValues);
      if (more) {
        view.state.setText("Prikazano prvih 200. Suzite trazenje motorom ili generacijom.");
      } else if (values.isEmpty()) {
        view.state.setText("Nema rezultata. Promijenite filtre.");
      } else {
        view.state.setText(
            "Odaberite redak; nepoznat zavrsetak raspona nije dokaz da se model jos proizvodi.");
      }
    } catch (RuntimeException exception) {
      Ui.error(view, exception);
    }
  }
}
