package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Iz servisne povijesti izvodi aktualno stanje planiranog održavanja vozila.
 *
 * <p>Servisna povijest je izvor istine: održavanje se prati tek kada za standardni rad postoji
 * stvarno evidentirana servisna stavka. Service priprema podatke, a izračun preostalog intervala
 * delegira domenskom {@code MaintenanceCalculatoru}.
 */
public class MaintenanceService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis za izračun održavanja.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public MaintenanceService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Dohvaća vozilo i vraća izračunate intervale održavanja na temelju njegove servisne povijesti.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return retci održavanja spremni za prikaz
   * @throws IllegalArgumentException ako vozilo ne pripada korisniku
   */
  public List<MaintenanceRow> list(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      return calculate(
          new CatalogRepository(entityManager),
          new ServiceRecordRepository(entityManager),
          ownerId,
          vehicle);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Računa MaintenanceRow rezultate koristeći katalog održavanja i povijesne servisne stavke.
   *
   * <p>Za svaki rad održavanja pronalazi posljednju izvedenu stavku, računa sljedeći datum i/ili
   * kilometražu, preostale dane i kilometre te relativni preostali interval. Radovi bez prethodno
   * evidentirane izvedbe ne prikazuju se kao praćeno održavanje.
   *
   * @param catalogRepository repository standardnih radova
   * @param serviceRecordRepository repository servisne povijesti
   * @param ownerId identifikator vlasnika
   * @param vehicle vozilo za koje se računa održavanje
   * @return izračunati retci održavanja
   */
  static List<MaintenanceRow> calculate(
      CatalogRepository catalogRepository,
      ServiceRecordRepository serviceRecordRepository,
      int ownerId,
      Vehicle vehicle) {
    Map<Integer, ServiceItem> latestItems = latestItems(serviceRecordRepository, ownerId, vehicle.getId());
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.now();
    List<MaintenanceRow> rows = new ArrayList<>();

    for (WorkDefinition work : catalogRepository.works(WorkCategory.MAINTENANCE)) {
      ServiceItem last = latestItems.get(work.getId());
      if (last == null) {
        continue;
      }

      LocalDate lastDate = last.getServiceRecord().getServiceDate();
      Integer lastMileage = last.getServiceRecord().getMileage();
      LocalDate nextDate = nextDate(lastDate, work.getIntervalMonths());
      Integer nextMileage = nextMileage(lastMileage, work.getIntervalKm());
      Integer remainingKm = remainingKm(nextMileage, vehicle.getCurrentMileage());
      Long remainingDays = remainingDays(nextDate, today);
      double remainingRatio = calculator.calculate(
              work.getIntervalKm(),
              work.getIntervalMonths(),
              lastDate,
              lastMileage,
              vehicle.getCurrentMileage(),
              today);

      rows.add(
          new MaintenanceRow(
              work.getName(),
              lastDate,
              lastMileage,
              nextDate,
              nextMileage,
              remainingKm,
              remainingDays,
              remainingRatio));
    }
    return rows;
  }

  /**
   * Za svaki kataloški rad pronalazi najnoviju servisnu stavku u povijesti vozila.
   *
   * @param serviceRecordRepository repository servisne povijesti
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return mapa work ID-a na posljednju evidentiranu stavku tog rada
   */
  private static Map<Integer, ServiceItem> latestItems(
      ServiceRecordRepository serviceRecordRepository, int ownerId, int vehicleId) {
    Map<Integer, ServiceItem> latest = new HashMap<>();
    for (ServiceItem item : serviceRecordRepository.historyItems(ownerId, vehicleId)) {
      if (!latest.containsKey(item.getWork().getId())) {
        latest.put(item.getWork().getId(), item);
      }
    }
    return latest;
  }


  /**
   * Računa koliko kilometara preostaje do sljedećeg kilometarskog intervala.
   *
   * @param nextMileage sljedeća ciljana kilometraža ili {@code null} ako se kilometri ne prate
   * @param currentMileage trenutačna kilometraža vozila
   * @return preostali kilometri, nula ako je interval dospio ili {@code null} ako nije primjenjivo
   */
  private static Integer remainingKm(Integer nextMileage, int currentMileage) {
    if (nextMileage == null) {
      return null;
    }

    int remaining = nextMileage - currentMileage;
    if (remaining < 0) {
      return 0;
    }
    return remaining;
  }

  /**
   * Računa koliko kalendarskih dana preostaje do sljedećeg vremenskog intervala.
   *
   * @param nextDate sljedeći ciljani datum ili {@code null} ako se vrijeme ne prati
   * @param today datum na koji se izračun radi
   * @return preostali dani, nula ako je interval dospio ili {@code null} ako nije primjenjivo
   */
  private static Long remainingDays(LocalDate nextDate, LocalDate today) {
    if (nextDate == null) {
      return null;
    }

    long remaining = ChronoUnit.DAYS.between(today, nextDate);
    if (remaining < 0) {
      return 0L;
    }
    return remaining;
  }

  /**
   * Iz posljednjeg datuma i intervala u mjesecima izračunava sljedeći datum održavanja.
   *
   * @param lastDate datum posljednjeg rada
   * @param months interval u mjesecima ili {@code null}
   * @return sljedeći datum ili {@code null} ako rad nema vremenski interval
   */
  private static LocalDate nextDate(LocalDate lastDate, Integer months) {
    if (months == null) {
      return null;
    }
    return lastDate.plusMonths(months);
  }

  /**
   * Iz posljednje kilometraže i kilometarskog intervala izračunava sljedeću ciljanu kilometražu.
   *
   * @param lastMileage kilometraža posljednjeg rada
   * @param intervalKm interval u kilometrima ili {@code null}
   * @return sljedeća kilometraža ili {@code null} ako rad nema kilometarski interval
   */
  private static Integer nextMileage(Integer lastMileage, Integer intervalKm) {
    if (intervalKm == null) {
      return null;
    }
    return lastMileage + intervalKm;
  }
}
