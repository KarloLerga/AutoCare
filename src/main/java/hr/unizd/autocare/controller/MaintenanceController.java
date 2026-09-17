package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.MaintenanceEstimate;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/** Učitava praćeno održavanje i procjenjuje bilo koje dostupno održavanje. */
public final class MaintenanceController {
  private final MainFrame frame;
  private final MaintenanceService maintenanceService;
  private final CatalogService catalogService;
  private final Session session;

  public MaintenanceController(
      MainFrame frame,
      MaintenanceService maintenanceService,
      CatalogService catalogService,
      Session session) {
    this.frame = frame;
    this.maintenanceService = maintenanceService;
    this.catalogService = catalogService;
    this.session = session;
    frame.maintenance.estimate.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            estimate();
          }
        });
  }

  public void load() {
    try {
      long ownerId = session.getOwnerId();
      long vehicleId = session.getActiveVehicle().getId();
      List<MaintenanceRow> tracked = maintenanceService.list(ownerId, vehicleId);
      List<WorkRow> available =
          catalogService.works(ownerId, vehicleId, WorkCategory.MAINTENANCE);
      frame.maintenance.setRows(tracked);
      frame.maintenance.setAvailableWorks(available);
      if (tracked.isEmpty()) {
        frame.maintenance.coverage.setText(
            "Nema praćenih stavki. Održavanje se počinje pratiti nakon evidentiranog servisa.");
      } else {
        frame.maintenance.coverage.setText("Praćenih održavanja: " + tracked.size());
      }
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }

  private void estimate() {
    WorkRow selected = frame.maintenance.selectedWork();
    if (selected == null) {
      Ui.info(frame.maintenance, "Odaberite održavanje.");
      return;
    }
    try {
      MaintenanceEstimate estimate =
          maintenanceService.estimate(
              session.getOwnerId(), session.getActiveVehicle().getId(), selected.getId());
      frame.maintenance.showEstimate(estimate);
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }
}
