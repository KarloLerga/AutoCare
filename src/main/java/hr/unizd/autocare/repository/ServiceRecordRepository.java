package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/** JPA dohvat i spremanje servisne povijesti. */
public class ServiceRecordRepository {
  private final EntityManager entityManager;

  /**
   * Stvara repozitorij za EntityManager trenutačne operacije.
   *
   * @param entityManager EntityManager kojim upravlja pozivajući servis
   */
  public ServiceRecordRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Predaje novi servis persistence kontekstu; transakcijom upravlja pozivajući servisni sloj.
   *
   * @param serviceRecord zapis koji treba spremiti
   */
  public void add(ServiceRecord serviceRecord) {
    entityManager.persist(serviceRecord);
  }

  /**
   * Vraća povijest servisa vozila ako ono pripada korisniku.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return servisni zapisi od najnovijeg prema najstarijem
   */
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

  /**
   * Vraća servis samo ako pripada vozilu korisnika; inače vraća {@code null}.
   *
   * @param ownerId primarni ključ vlasnika
   * @param serviceId primarni ključ servisa
   * @return servis vlasnika ili {@code null}
   */
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

  /**
   * Vraća servisne stavke vozila od najnovije prema najstarijoj.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return stavke iz servisne povijesti
   */
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

  /**
   * Vraća zbroj stvarno plaćenih cijena servisnih stavki vozila.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return ukupno evidentirani stvarni trošak ili nula
   */
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
