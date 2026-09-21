package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import java.util.List;

/** Dohvat i spremanje servisne povijesti. */
public interface ServiceRecordRepository {
  void add(ServiceRecord serviceRecord);

  List<ServiceRecord> list(long ownerId, long vehicleId);

  ServiceRecord findForOwner(long ownerId, long serviceId);

  List<ServiceItem> historyItems(long ownerId, long vehicleId);

  CostSummary total(long ownerId, long vehicleId);

  void deleteForVehicle(long vehicleId);
}
