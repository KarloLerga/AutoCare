package hr.unizd.autocare.controller;

import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/** Kaskadni katalog. Svaka promjena roditelja odmah uklanja prethodno odabrani ID. */
public final class VehicleFormController {
  private final VehicleForm view;
  private final CatalogService service;
  private final UiTasks tasks;

  public VehicleFormController(VehicleForm view, CatalogService service) {
    this.view = view;
    this.service = service;
    tasks = new UiTasks();
    view.year.addChangeListener(
        e -> {
          if (!view.updating) {
            loadMakes();
          }
        });
    view.make.addActionListener(
        e -> {
          if (!view.updating) {
            loadModels();
          }
        });
    view.model.addActionListener(
        e -> {
          if (!view.updating) {
            loadVariants();
          }
        });
    view.find.addActionListener(e -> loadVariants());
    view.search.addActionListener(e -> loadVariants());
  }

  public void loadMakes() {
    try {
      int year = Ui.integer(view.year);
      view.updating = true;
      view.make.removeAllItems();
      view.model.removeAllItems();
      view.variants.setRows(List.of());
      view.updating = false;
      view.state.setText("Ucitavanje marki...");
      tasks.read(
          view,
          () -> service.makes(year),
          values -> {
            view.updating = true;
            view.make.setModel(new DefaultComboBoxModel<>(values.toArray(String[]::new)));
            view.make.setSelectedIndex(-1);
            view.updating = false;
            view.state.setText(
                values.isEmpty()
                    ? "Nema kataloga za ovu godinu. Provjerite seed."
                    : "Odaberite marku.");
          });
    } catch (RuntimeException ex) {
      Ui.error(view, ex);
    }
  }

  private void loadModels() {
    String make = (String) view.make.getSelectedItem();
    view.updating = true;
    view.model.removeAllItems();
    view.variants.setRows(List.of());
    view.updating = false;
    if (make == null) {
      return;
    }
    int year = Ui.integer(view.year);
    tasks.read(
        view,
        () -> service.models(year, make),
        values -> {
          view.updating = true;
          view.model.setModel(new DefaultComboBoxModel<>(values.toArray(String[]::new)));
          view.model.setSelectedIndex(-1);
          view.updating = false;
          view.state.setText("Odaberite model.");
        });
  }

  private void loadVariants() {
    String make = (String) view.make.getSelectedItem(),
        model = (String) view.model.getSelectedItem(),
        query = view.search.getText();
    view.variants.setRows(List.of());
    if (make == null || model == null) {
      return;
    }
    int year = Ui.integer(view.year);
    view.state.setText("Ucitavanje varijanti...");
    tasks.read(
        view,
        () -> service.variants(year, make, model, query),
        values -> {
          boolean more = values.size() > 200;
          view.variants.setRows(more ? values.subList(0, 200) : values);
          view.state.setText(
              more
                  ? "Prikazano prvih 200. Suzite trazenje motorom ili generacijom."
                  : values.isEmpty()
                      ? "Nema rezultata. Promijenite filtre."
                      : "Odaberite redak; nepoznat zavrsetak raspona nije dokaz da se model jos"
                          + " proizvodi.");
        });
  }
}
