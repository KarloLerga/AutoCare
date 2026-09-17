package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/** Dodavanje, uredjivanje, aktiviranje i brisanje vozila. */
public final class VehiclesController {
  private final MainFrame frame;
  private final VehicleService vehicleService;
  private final CatalogService catalogService;
  private final Session session;
  private final AppEvents events;

  public VehiclesController(
      MainFrame frame,
      VehicleService vehicleService,
      CatalogService catalogService,
      Session session,
      AppEvents events) {
    this.frame = frame;
    this.vehicleService = vehicleService;
    this.catalogService = catalogService;
    this.session = session;
    this.events = events;
    registerListeners();
  }

  private void registerListeners() {
    frame.vehicles.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            edit(null);
          }
        });
    frame.vehicles.edit.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            VehicleRow vehicle = selected();
            if (vehicle != null) {
              edit(vehicle);
            }
          }
        });
    frame.vehicles.activate.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            activate();
          }
        });
    frame.vehicles.delete.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            delete();
          }
        });
  }

  public void load() {
    try {
      frame.vehicles.setRows(vehicleService.list(session.owner()));
    } catch (RuntimeException exception) {
      Ui.error(frame.vehicles, exception);
    }
  }

  private VehicleRow selected() {
    VehicleRow vehicle = frame.vehicles.selected();
    if (vehicle == null) {
      Ui.info(frame, "Odaberite vozilo.");
    }
    return vehicle;
  }

  private void activate() {
    VehicleRow vehicle = selected();
    if (vehicle == null) {
      return;
    }
    try {
      vehicleService.activate(session.owner(), vehicle.getId());
      events.publish(AppEvent.ACTIVE_VEHICLE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void edit(VehicleRow vehicle) {
    if (vehicle == null) {
      showEditor(null, true);
      return;
    }
    try {
      boolean editable = vehicleService.identityEditable(session.owner(), vehicle.getId());
      showEditor(vehicle, editable);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void showEditor(final VehicleRow vehicle, boolean identityEditable) {
    JDialog dialog =
        new JDialog(
            frame,
            vehicle == null ? "Dodaj vozilo" : "Uredi vozilo",
            Dialog.ModalityType.APPLICATION_MODAL);
    VehicleForm form = new VehicleForm();
    VehicleFormController picker = new VehicleFormController(form, catalogService);
    JButton save = Ui.button("Spremi vozilo", true);
    JButton cancel = Ui.button("Odustani", false);
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(form, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);
    dialog.setContentPane(root);
    dialog.setSize(900, 660);
    dialog.setLocationRelativeTo(frame);
    save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              VehicleInput input = form.input();
              if (vehicle == null) {
                vehicleService.add(session.owner(), input);
              } else {
                vehicleService.update(session.owner(), vehicle.getId(), input);
              }
              dialog.dispose();
              events.publish(AppEvent.VEHICLE_CHANGED);
            } catch (RuntimeException exception) {
              Ui.error(dialog, exception);
            }
          }
        });
    cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            dialog.dispose();
          }
        });
    Ui.escape(dialog);
    if (vehicle == null) {
      picker.loadMakes();
    } else {
      form.existing(vehicle, identityEditable);
    }
    dialog.setVisible(true);
  }

  private void delete() {
    VehicleRow vehicle = selected();
    if (vehicle == null) {
      return;
    }
    if (frame.vehicles.rows().size() <= 1) {
      Ui.info(frame, "Posljednje vozilo nije moguce obrisati.");
      return;
    }
    if (!Ui.confirm(
        frame,
        "Trajno obrisati vozilo #"
            + vehicle.getId()
            + ", njegove servise i probleme?")) {
      return;
    }
    try {
      vehicleService.delete(session.owner(), vehicle.getId());
      events.publish(AppEvent.VEHICLE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }
}
