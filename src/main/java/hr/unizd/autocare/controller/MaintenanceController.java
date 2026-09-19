package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.util.List;

/** Učitava održavanje koje se prati iz servisne povijesti aktivnog vozila. */
public final class MaintenanceController {
  private final MainFrame frame;
  private final MaintenanceService maintenanceService;
  private final Session session;

  public MaintenanceController(
      MainFrame frame, MaintenanceService maintenanceService, Session session) {
    this.frame = frame;
    this.maintenanceService = maintenanceService;
    this.session = session;
  }

  public void load() {
    try {
      List<MaintenanceRow> tracked =
          maintenanceService.list(
              session.getOwnerId(), session.getActiveVehicle().getId());
      frame.maintenance.setRows(tracked);
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
}
