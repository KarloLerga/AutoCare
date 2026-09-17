package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Pregled odrzavanja i jednostavan zbroj odabranih procjena. */
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
            showEstimate();
          }
        });
  }

  public void load() {
    try {
      List<MaintenanceRow> rows =
          maintenanceService.list(session.owner(), session.active().getId());
      frame.maintenance.setRows(rows);

      if (rows.isEmpty()) {
        frame.maintenance.coverage.setText("Nema podataka o odrzavanju za ovu varijantu.");
      } else {
        frame.maintenance.coverage.setText(
            "Prikazano stavki: " + rows.size() + ". Procjene su informativne.");
      }
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }

  private void showEstimate() {
    int[] selectedRows = frame.maintenance.table.getSelectedRows();
    if (selectedRows.length == 0) {
      Ui.info(frame, "Odaberite jedan ili vise redaka.");
      return;
    }

    List<BigDecimal> prices = new ArrayList<>();
    StringBuilder details = new StringBuilder();

    for (int selectedRow : selectedRows) {
      int modelRow = frame.maintenance.table.convertRowIndexToModel(selectedRow);
      MaintenanceRow maintenanceRow = frame.maintenance.rows().get(modelRow);

      prices.add(maintenanceRow.getPrice());
      details.append("\n");
      details.append(maintenanceRow.getName());
      details.append(": ");
      details.append(Ui.estimate(maintenanceRow.getPrice()));

      if (maintenanceRow.getPriceNote() != null) {
        details.append(" / ");
        details.append(maintenanceRow.getPriceNote());
      }
    }

    CostSummary summary = CostSummary.of(prices);
    String message = "Informativna procjena: " + Ui.estimate(summary.getKnownTotal());

    if (summary.getUnknownCount() > 0) {
      message += " + stavke bez poznate procjene";
    }

    Ui.info(frame, message + details);
  }
}
