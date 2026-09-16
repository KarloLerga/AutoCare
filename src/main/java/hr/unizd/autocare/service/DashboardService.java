package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.service.TransactionRunner;
import java.time.Clock;
import java.util.List;

/** Podaci za dashboard aktivnog vozila. */
public final class DashboardService {

    private final TransactionRunner transactions;
    private final Clock clock;

    public DashboardService(
            TransactionRunner transactions,
            Clock clock) {

        this.transactions = transactions;
        this.clock = clock;
    }

    public Dashboard get(
            long ownerId,
            long vehicleId) {

        return transactions.read(repositories -> {
            Vehicle vehicle =
                    repositories.vehicles()
                            .requireOwned(ownerId, vehicleId);

            List<MaintenanceRow> maintenance =
                    MaintenanceService.calculate(
                            repositories,
                            ownerId,
                            vehicle,
                            clock);

            int due = 0;
            int soon = 0;
            int noData = 0;

            for (MaintenanceRow row : maintenance) {
                if (row.getStatus()
                        == MaintenanceStatus.DUE) {
                    due++;
                } else if (row.getStatus()
                        == MaintenanceStatus.SOON) {
                    soon++;
                } else if (row.getStatus()
                        == MaintenanceStatus.NO_DATA) {
                    noData++;
                }
            }

            return new Dashboard(
                    Mapping.vehicle(
                            vehicle,
                            vehicleId),
                    repositories.services()
                            .total(
                                    ownerId,
                                    vehicleId),
                    repositories.problems()
                            .openCount(
                                    ownerId,
                                    vehicleId),
                    due,
                    soon,
                    noData,
                    maintenance.size());
        });
    }
}
