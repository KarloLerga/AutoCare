package hr.unizd.autocare.service;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.repository.Repositories;
import java.time.*;
import java.util.*;
/** Stanje odrzavanja uvijek proizlazi iz pravila i servisne povijesti. */
public final class MaintenanceService {
    private final TransactionRunner tx;
    private final Clock clock;
    public MaintenanceService(TransactionRunner tx, Clock clock) {
        this.tx=tx;
        this.clock=clock;
    }
    public List<MaintenanceRow> list(long owner, long vehicle) {
        return tx.read(r->calculate(r, owner, r.vehicles().requireOwned(owner, vehicle), clock));
    }
    static List<MaintenanceRow> calculate(Repositories r, long owner, Vehicle vehicle, Clock clock) {
        Map<Long, ServiceItem> latest=new HashMap<>();
        for(ServiceItem item:r.services().historyItems(owner, vehicle.getId()))latest.putIfAbsent(item.getWork().getId(), item);
        List<MaintenanceRow> rows=new ArrayList<>();
        MaintenanceCalculator calc=new MaintenanceCalculator();
        for(VehicleWorkRule rule:r.catalog().rules(vehicle.getVariant().getId())) {
            WorkDefinition w=rule.getWork();
            if(w.getCategory()!=WorkCategory.MAINTENANCE)continue;
            ServiceItem item=latest.get(w.getId());
            LocalDate lastDate=item==null?null:item.getServiceRecord().getDate();
            Integer lastKm=item==null?null:item.getServiceRecord().getMileage();
            LocalDate nextDate=lastDate==null||rule.getIntervalMonths()==null?null:lastDate.plusMonths(rule.getIntervalMonths());
            Integer nextKm=lastKm==null||rule.getIntervalKm()==null?null:Math.addExact(lastKm, rule.getIntervalKm());
            boolean specific=rule.getEstimatedPrice()!=null;
            rows.add(new MaintenanceRow(w.getId(), w.getCode(), w.getName(), lastDate, lastKm, nextDate, nextKm, calc.calculate(rule.getScheduleKind(), rule.getIntervalKm(), rule.getIntervalMonths(), lastDate, lastKm, vehicle.getCurrentMileage(), LocalDate.now(clock)), rule.getEstimatedPrice(), rule.getIntervalSource(), rule.getEstimateNote(), nextKm==null?null:nextKm-vehicle.getCurrentMileage(), nextDate==null?null:java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(clock), nextDate)));
        }
        return List.copyOf(rows);
    }
}
