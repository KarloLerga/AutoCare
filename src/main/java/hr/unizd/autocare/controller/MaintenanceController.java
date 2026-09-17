package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.model.Data.MaintenanceEstimate;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/** Učitava praćeno održavanje i procjenjuje odabranu konkretnu stavku. */
public final class MaintenanceController {
  private final MainFrame frame;
  private final MaintenanceService maintenanceService;
  private final Session session;

  public MaintenanceController(
      MainFrame frame, MaintenanceService maintenanceService, Session session) {
    this.frame = frame;
    this.maintenanceService = maintenanceService;
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
      List<MaintenanceRow> rows =
          maintenanceService.list(session.owner(), session.active().getId());
      frame.maintenance.setRows(rows);
      frame.maintenance.coverage.setText(
          rows.isEmpty()
              ? "Nema praćenih stavki. Održavanje se pojavi nakon što se rad zabilježi u servisu."
              : "Prikazano praćenih stavki: " + rows.size());
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }

  private void estimate() {
    MaintenanceRow selected = frame.maintenance.selectedWork();
    if (selected == null) {
      Ui.info(frame.maintenance, "Odaberite praćeni rad.");
      return;
    }
    try {
      MaintenanceEstimate estimate =
          maintenanceService.estimate(
              session.owner(), session.active().getId(), selected.getWorkId());
      frame.maintenance.showEstimate(estimate);
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }
}
