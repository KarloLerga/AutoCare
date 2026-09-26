package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.MileageDialog;
import hr.unizd.autocare.view.VehicleDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VehiclesController {
  private final MainFrame frame;
  private final VehicleService vehicleService;
  private final CatalogService catalogService;
  private final Session session;
  private final Subject subject;

  public VehiclesController(
      MainFrame frame,
      VehicleService vehicleService,
      CatalogService catalogService,
      Session session,
      Subject subject) {
    this.frame = frame;
    this.vehicleService = vehicleService;
    this.catalogService = catalogService;
    this.session = session;
    this.subject = subject;
    registerListeners();
  }

  private void registerListeners() {
    frame.vehicles.add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        showAdd();
      }
    });

    frame.vehicles.edit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        showMileageEditor();
      }
    });

    frame.vehicles.activate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        activate();
      }
    });
  }

  public void load() {
    try {
      Integer activeVehicleId = null;
      if (session.getActiveVehicle() != null) {
        activeVehicleId = session.getActiveVehicle().getId();
      }
      frame.vehicles.setRows(vehicleService.list(session.getOwnerId()), activeVehicleId);
    } catch (RuntimeException exception) {
      Ui.error(frame.vehicles, exception);
    }
  }

  private Vehicle selected() {
    Vehicle vehicle = frame.vehicles.selected();
    if (vehicle == null) {
      Ui.info(frame, "Odaberite vozilo.");
    }
    return vehicle;
  }

  private void showAdd() {
    final VehicleDialog dialog = new VehicleDialog(frame);
    VehicleFormController formController = new VehicleFormController(dialog.form, catalogService);
    formController.loadMakes();

    dialog.save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          VehicleVariant variant = dialog.form.selectedVariant();
          if (variant == null) {
            throw new IllegalArgumentException("Odaberite točnu varijantu vozila.");
          }

          vehicleService.add(
              session.getOwnerId(),
              variant.getId(),
              dialog.form.getSelectedYear(),
              dialog.form.getMileage());
          dialog.dispose();
          subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
        } catch (RuntimeException exception) {
          Ui.error(dialog, exception);
        }
      }
    });

    dialog.cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        dialog.dispose();
      }
    });

    dialog.setVisible(true);
  }

  private void showMileageEditor() {
    Vehicle vehicle = selected();
    if (vehicle == null) {
      return;
    }

    final MileageDialog dialog = new MileageDialog(frame, vehicle.getCurrentMileage());

    dialog.save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          vehicleService.updateMileage(session.getOwnerId(), vehicle.getId(), Ui.mileage(dialog.mileage));
          dialog.dispose();
          subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
        } catch (RuntimeException exception) {
          Ui.error(dialog, exception);
        }
      }
    });

    dialog.cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        dialog.dispose();
      }
    });

    dialog.setVisible(true);
  }

  private void activate() {
    Vehicle vehicle = selected();
    if (vehicle == null) {
      return;
    }

    try {
      vehicleService.activate(session.getOwnerId(), vehicle.getId());
      subject.notifyObservers(AppEvent.ACTIVE_VEHICLE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }
}
