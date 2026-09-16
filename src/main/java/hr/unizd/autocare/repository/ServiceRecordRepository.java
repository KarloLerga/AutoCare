package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import java.util.List;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface ServiceRecordRepository {
  void add(ServiceRecord record);

  List<ServiceRecord> page(long owner, long vehicle, int offset, int limit);

  ServiceRecord requireOwned(long owner, long id);

  List<ServiceItem> historyItems(long owner, long vehicle);

  CostSummary total(long owner, long vehicle);

  void deleteForVehicle(long vehicle);
}
