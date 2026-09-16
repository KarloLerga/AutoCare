package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaServiceRecordRepository implements ServiceRecordRepository {
  private final EntityManager em;

  public JpaServiceRecordRepository(EntityManager em) {
    this.em = em;
  }

  public void add(ServiceRecord s) {
    em.persist(s);
  }

  public List<ServiceRecord> page(long owner, long vehicle, int offset, int limit) {
    List<Long> ids =
        em.createQuery(
                "select s.id from ServiceRecord s where s.vehicle.owner.id=:o and s.vehicle.id=:v"
                    + " order by s.serviceDate desc,s.mileage desc,s.id desc",
                Long.class)
            .setParameter("o", owner)
            .setParameter("v", vehicle)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    if (ids.isEmpty()) {
      return List.of();
    }
    return em.createQuery(
            "select distinct s from ServiceRecord s left join fetch s.items i left join fetch"
                + " i.work where s.id in :ids and s.vehicle.owner.id=:o order by s.serviceDate"
                + " desc,s.mileage desc,s.id desc",
            ServiceRecord.class)
        .setParameter("ids", ids)
        .setParameter("o", owner)
        .getResultList();
  }

  public ServiceRecord requireOwned(long owner, long id) {
    return em.createQuery(
            "select distinct s from ServiceRecord s left join fetch s.items i left join fetch"
                + " i.work where s.id=:id and s.vehicle.owner.id=:o",
            ServiceRecord.class)
        .setParameter("id", id)
        .setParameter("o", owner)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new AppException(AppException.Kind.NOT_FOUND, "Servis nije pronadjen."));
  }

  public List<ServiceItem> historyItems(long owner, long vehicle) {
    return em.createQuery(
            "select i from ServiceItem i join fetch i.work join fetch i.serviceRecord s where"
                + " s.vehicle.owner.id=:o and s.vehicle.id=:v order by s.serviceDate desc,s.mileage"
                + " desc,s.id desc",
            ServiceItem.class)
        .setParameter("o", owner)
        .setParameter("v", vehicle)
        .getResultList();
  }

  public CostSummary total(long owner, long vehicle) {
    Object[] a =
        em.createQuery(
                "select sum(i.actualPrice),count(i),count(i.actualPrice) from ServiceItem i where"
                    + " i.serviceRecord.vehicle.owner.id=:o and i.serviceRecord.vehicle.id=:v",
                Object[].class)
            .setParameter("o", owner)
            .setParameter("v", vehicle)
            .getSingleResult();
    return new CostSummary(
        (java.math.BigDecimal) a[0], ((Number) a[1]).longValue() - ((Number) a[2]).longValue());
  }

  public void deleteForVehicle(long vehicle) {
    em.createQuery(
            "delete from ServiceItem i where i.serviceRecord.id in (select s.id from ServiceRecord"
                + " s where s.vehicle.id=:v)")
        .setParameter("v", vehicle)
        .executeUpdate();
    em.createQuery("delete from ServiceRecord s where s.vehicle.id=:v")
        .setParameter("v", vehicle)
        .executeUpdate();
  }
}
