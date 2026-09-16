package hr.unizd.autocare.service;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.model.Data.*;
import java.time.*;
import java.util.*;
/** Jedna read transakcija za konzistentan sazetak; bez ugnijezdenih service poziva. */
public final class DashboardService {
    private final TransactionRunner tx;
    private final Clock clock;
    public DashboardService(TransactionRunner tx, Clock clock) {
        this.tx=tx;
        this.clock=clock;
    }
    public Dashboard get(long owner, long vehicle) {
        return tx.read(r-> {
            Vehicle v=r.vehicles().requireOwned(owner, vehicle);
            List<MaintenanceRow> rows=MaintenanceService.calculate(r, owner, v, clock);
            int due=0, soon=0, unknown=0;
            for(MaintenanceRow m:rows) {
                if(m.getStatus()==MaintenanceStatus.DUE)due++;
                else if(m.getStatus()==MaintenanceStatus.SOON)soon++;
                else if(m.getStatus()==MaintenanceStatus.UNKNOWN_HISTORY||m.getStatus()==MaintenanceStatus.UNKNOWN_INTERVAL)unknown++;
            }
            return new Dashboard(Mapping.vehicle(v, vehicle), r.services().total(owner, vehicle), r.problems().openCount(owner, vehicle), due, soon, unknown, rows.size());
        });
    }
}
