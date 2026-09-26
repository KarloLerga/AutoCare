package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/** JPA dohvat i spremanje servisne povijesti. */
public class ServiceRecordRepository {
  private final EntityManager entityManager;

  public ServiceRecordRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void add(ServiceRecord serviceRecord) {
    entityManager.persist(serviceRecord);
  }

  public List<ServiceRecord> list(int ownerId, int vehicleId) {
    return entityManager
        .createQuery(
            "select serviceRecord from ServiceRecord serviceRecord "
                + "where serviceRecord.vehicle.id=:vehicleId "
                + "and serviceRecord.vehicle.owner.id=:ownerId "
                + "order by serviceRecord.serviceDate desc, "
                + "serviceRecord.mileage desc, serviceRecord.id desc",
            ServiceRecord.class)
        .setParameter("vehicleId", vehicleId)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public ServiceRecord findForOwner(int ownerId, int serviceId) {
    List<ServiceRecord> serviceRecords = entityManager
            .createQuery(
                "select serviceRecord from ServiceRecord serviceRecord "
                    + "where serviceRecord.id=:serviceId "
                    + "and serviceRecord.vehicle.owner.id=:ownerId",
                ServiceRecord.class)
            .setParameter("serviceId", serviceId)
            .setParameter("ownerId", ownerId)
            .setMaxResults(1)
            .getResultList();

    if (serviceRecords.isEmpty()) {
      return null;
    }
    return serviceRecords.get(0);
  }

  public List<ServiceItem> historyItems(int ownerId, int vehicleId) {
    return entityManager
        .createQuery(
            "select serviceItem from ServiceItem serviceItem "
                + "where serviceItem.serviceRecord.vehicle.owner.id=:ownerId "
                + "and serviceItem.serviceRecord.vehicle.id=:vehicleId "
                + "order by serviceItem.serviceRecord.serviceDate desc, "
                + "serviceItem.serviceRecord.mileage desc, "
                + "serviceItem.serviceRecord.id desc",
            ServiceItem.class)
        .setParameter("ownerId", ownerId)
        .setParameter("vehicleId", vehicleId)
        .getResultList();
  }

  public BigDecimal total(int ownerId, int vehicleId) {
    BigDecimal total = entityManager
            .createQuery(
                "select sum(serviceItem.actualPrice) from ServiceItem serviceItem "
                    + "where serviceItem.serviceRecord.vehicle.owner.id=:ownerId "
                    + "and serviceItem.serviceRecord.vehicle.id=:vehicleId",
                BigDecimal.class)
            .setParameter("ownerId", ownerId)
            .setParameter("vehicleId", vehicleId)
            .getSingleResult();

    if (total == null) {
      return BigDecimal.ZERO;
    }
    return total;
  }
}
