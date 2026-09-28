package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repository za JPA pristup servisnim zapisima, stavkama i agregatnim troškovima.
 *
 * <p>Upiti servisnu povijest ograničavaju identifikatorima vozila i vlasnika. Repository ne otvara
 * EntityManager ni transakcije; koristi kontekst koji mu preda Service sloj.
 */
public class ServiceRecordRepository {
  /** EntityManager trenutačnog Service use-casea kroz koji se izvode upiti i spremanje. */
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
   * Dodaje novi servis u persistence context trenutačnog EntityManagera.
   *
   * <p>Povezane nove stavke prate postojeću JPA postavku {@code CascadeType.PERSIST} na servisu, pa
   * ih Service ne mora zasebno spremati.
   *
   * @param serviceRecord novi servisni zapis
   */
  public void add(ServiceRecord serviceRecord) {
    entityManager.persist(serviceRecord);
  }

  /**
   * Dohvaća servisnu povijest vozila samo unutar vlasničkog konteksta korisnika.
   *
   * <p>Rezultati su poredani po datumu servisa, kilometraži i ID-u, silazno, tako da se najnoviji
   * zapisi pojavljuju prvi.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return servisni zapisi u redoslijedu upita; prazna lista ako vozilo nema povijest
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
   * Dohvaća najviše jedan servis samo ako pripada vozilu zadanog korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @param serviceId identifikator servisa
   * @return servis ili {@code null} ako ne postoji ili ne pripada korisniku
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
   * Dohvaća servisne stavke za analizu održavanja jednog vozila u korisničkom kontekstu.
   *
   * <p>JPQL ograničava rezultate i po vlasniku vozila i po konkretnom vozilu, a vraća cijele
   * {@link ServiceItem} entitete jer izračun treba doći do rada, datuma i kilometraže servisa.
   * Sortiranje po datumu, kilometraži i ID-u silazno osigurava da se najnoviji rad iste vrste
   * pojavi prvi u {@code MaintenanceService.latestItems}.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return povijesne servisne stavke sortirane tako da se najnovija izvedba rada može prepoznati
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
   * Računa zbroj stvarno plaćenih servisnih stavki za vozilo.
   *
   * <p>{@code SUM} upit daje jednu skalarnu vrijednost; kada nema stavki, rezultat može biti
   * {@code null}, što pozivatelj pretvara u nulu.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return ukupni evidentirani stvarni servisni trošak
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
