package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;

/** Učitava održavanje koje se prati iz servisne povijesti aktivnog vozila. */
public class MaintenanceController {
  private final MainFrame frame;
  private final MaintenanceService maintenanceService;
  private final Session session;

  public MaintenanceController(MainFrame frame, MaintenanceService maintenanceService, Session session) {
    this.frame = frame;
    this.maintenanceService = maintenanceService;
    this.session = session;
  }

  public void load() {
    try {
      frame.maintenance.setRows(maintenanceService.list(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }
}
