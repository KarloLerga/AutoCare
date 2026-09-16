package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.view.components.Ui;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Pregled odrzavanja i jednostavan zbroj odabranih procjena. */
public final class MaintenanceController {

    private final MainFrame frame;
    private final MaintenanceService service;
    private final Session session;
    private final UiTasks tasks = new UiTasks();

    public MaintenanceController(
            MainFrame frame,
            MaintenanceService service,
            Session session) {

        this.frame = frame;
        this.service = service;
        this.session = session;

        frame.maintenance.estimate.addActionListener(
                event -> showEstimate());
    }

    public void load() {
        long ownerId = session.owner();
        long vehicleId = session.active().getId();

        tasks.read(
                frame.maintenance,
                () -> service.list(
                        ownerId,
                        vehicleId),
                rows -> {
                    frame.maintenance.table.setRows(rows);

                    frame.maintenance.coverage.setText(
                            rows.isEmpty()
                                    ? "Nema podataka o odrzavanju za ovu varijantu."
                                    : "Prikazano stavki: "
                                            + rows.size()
                                            + ". Procjene su informativne.");
                });
    }

    private void showEstimate() {
        int[] selectedRows =
                frame.maintenance.table
                        .table()
                        .getSelectedRows();

        if (selectedRows.length == 0) {
            Ui.info(
                    frame,
                    "Odaberite jedan ili vise redaka.");
            return;
        }

        List<BigDecimal> prices =
                new ArrayList<>();

        StringBuilder details =
                new StringBuilder();

        for (int selectedRow : selectedRows) {
            int modelRow =
                    frame.maintenance.table
                            .table()
                            .convertRowIndexToModel(
                                    selectedRow);

            MaintenanceRow row =
                    frame.maintenance.table
                            .rows()
                            .get(modelRow);

            prices.add(row.getPrice());

            details.append("\n");
            details.append(row.getName());
            details.append(": ");
            details.append(
                    EstimateFormat.display(
                            row.getPrice()));

            if (row.getPriceNote() != null) {
                details.append(" / ");
                details.append(row.getPriceNote());
            }
        }

        CostSummary summary =
                CostSummary.of(prices);

        String message =
                "Informativna procjena: "
                        + EstimateFormat.display(
                                summary.getKnownTotal());

        if (summary.getUnknownCount() > 0) {
            message +=
                    " + stavke bez poznate procjene";
        }

        message += details;

        Ui.info(frame, message);
    }
}
