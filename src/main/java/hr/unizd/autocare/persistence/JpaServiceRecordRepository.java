package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/** JPA pristup servisnoj povijesti. */
public final class JpaServiceRecordRepository implements ServiceRecordRepository {
  private final EntityManager entityManager;

  public JpaServiceRecordRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void add(ServiceRecord serviceRecord) {
    entityManager.persist(serviceRecord);
  }

  @Override
  public List<ServiceRecord> list(long ownerId, long vehicleId) {
    return entityManager
        .createQuery(
            "select distinct serviceRecord from ServiceRecord serviceRecord "
                + "left join fetch serviceRecord.items serviceItem "
                + "left join fetch serviceItem.work where serviceRecord.vehicle.id=:vehicleId "
                + "and serviceRecord.vehicle.owner.id=:ownerId "
                + "order by serviceRecord.serviceDate desc,serviceRecord.mileage desc,serviceRecord.id desc",
            ServiceRecord.class)
        .setParameter("vehicleId", vehicleId)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  @Override
  public ServiceRecord findForOwner(long ownerId, long serviceId) {
    List<ServiceRecord> serviceRecords =
        entityManager
            .createQuery(
                "select distinct serviceRecord from ServiceRecord serviceRecord "
                    + "left join fetch serviceRecord.items serviceItem "
                    + "left join fetch serviceItem.work where serviceRecord.id=:serviceId "
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

  @Override
  public List<ServiceItem> historyItems(long ownerId, long vehicleId) {
    return entityManager
        .createQuery(
            "select serviceItem from ServiceItem serviceItem join fetch serviceItem.work "
                + "join fetch serviceItem.serviceRecord serviceRecord "
                + "where serviceRecord.vehicle.owner.id=:ownerId "
                + "and serviceRecord.vehicle.id=:vehicleId "
                + "order by serviceRecord.serviceDate desc,serviceRecord.mileage desc,"
                + "serviceRecord.id desc",
            ServiceItem.class)
        .setParameter("ownerId", ownerId)
        .setParameter("vehicleId", vehicleId)
        .getResultList();
  }

  @Override
  public CostSummary total(long ownerId, long vehicleId) {
    List<BigDecimal> prices =
        entityManager
            .createQuery(
                "select serviceItem.actualPrice from ServiceItem serviceItem where "
                    + "serviceItem.serviceRecord.vehicle.owner.id=:ownerId and "
                    + "serviceItem.serviceRecord.vehicle.id=:vehicleId",
                BigDecimal.class)
            .setParameter("ownerId", ownerId)
            .setParameter("vehicleId", vehicleId)
            .getResultList();
    return CostSummary.of(prices);
  }

  @Override
  public void deleteForVehicle(long vehicleId) {
    entityManager
        .createQuery(
            "delete from ServiceItem serviceItem where serviceItem.serviceRecord.id in "
                + "(select serviceRecord.id from ServiceRecord serviceRecord "
                + "where serviceRecord.vehicle.id=:vehicleId)")
        .setParameter("vehicleId", vehicleId)
        .executeUpdate();
    entityManager
        .createQuery("delete from ServiceRecord serviceRecord where serviceRecord.vehicle.id=:vehicleId")
        .setParameter("vehicleId", vehicleId)
        .executeUpdate();
  }
}
