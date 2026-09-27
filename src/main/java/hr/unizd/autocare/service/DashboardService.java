package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Priprema sažetak podataka koji Dashboard prikazuje za aktivno vozilo.
 *
 * <p>Service spaja podatke iz vozila, servisne povijesti, problema i izračuna održavanja u jedan
 * jednostavan Dashboard objekt spreman za prikaz.
 */
public class DashboardService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public DashboardService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Dohvaća sva četiri glavna Dashboard pokazatelja za vozilo.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return sažetak vozila, stvarnih troškova, otvorenih problema i najbližeg održavanja
   * @throws IllegalArgumentException ako vozilo ne pripada korisniku
   */
  public Dashboard get(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      CatalogRepository catalogRepository = new CatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new ProblemRepository(entityManager);

      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }

      List<MaintenanceRow> maintenance =
          MaintenanceService.calculate(catalogRepository, serviceRecordRepository, ownerId, vehicle);

      MaintenanceRow next = null;
      for (MaintenanceRow row : maintenance) {
        if (next == null || comesBefore(row, next)) {
          next = row;
        }
      }

      return new Dashboard(
          vehicle,
          serviceRecordRepository.total(ownerId, vehicleId),
          problemRepository.openCount(ownerId, vehicleId),
          next);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Određuje koji od dva MaintenanceRow zapisa predstavlja bliže održavanje.
   *
   * <p>Primarno uspoređuje relativni preostali interval, a naziv koristi kao stabilan sekundarni
   * kriterij kada su vrijednosti jednake.
   *
   * @param first prvi kandidat
   * @param second drugi kandidat
   * @return {@code true} ako prvi kandidat treba biti prikazan kao bliže održavanje
   */
  private static boolean comesBefore(MaintenanceRow first, MaintenanceRow second) {
    if (first.getRemainingRatio() != second.getRemainingRatio()) {
      return first.getRemainingRatio() < second.getRemainingRatio();
    }
    return first.getName().compareTo(second.getName()) < 0;
  }
}
