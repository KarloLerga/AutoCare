package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.domain.WorkPriceRange;
import hr.unizd.autocare.model.Data.CatalogRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

/** Čitanje kataloga vozila, zahvata i informativnih raspona cijena. */
public final class CatalogService {
  private final EntityManagerFactory entityManagerFactory;

  public CatalogService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<String> makes() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new JpaCatalogRepository(entityManager).makes();
    } finally {
      entityManager.close();
    }
  }

  public List<String> models(String make) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new JpaCatalogRepository(entityManager).models(make);
    } finally {
      entityManager.close();
    }
  }

  public List<Integer> years(String make, String model) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new JpaCatalogRepository(entityManager).years(make, model);
    } finally {
      entityManager.close();
    }
  }

  public List<VehicleVariant> variants(String make, String model, int year) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new JpaCatalogRepository(entityManager).variants(make, model, year);
    } finally {
      entityManager.close();
    }
  }

  public List<WorkDefinition> works(WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new JpaCatalogRepository(entityManager).works(category);
    } finally {
      entityManager.close();
    }
  }

  public List<CatalogRow> catalog(
      long ownerId,
      long vehicleId,
      String searchText,
      CatalogCategory selectedCategory) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }

      String search = "";
      if (searchText != null) {
        search = searchText.trim().toLowerCase();
      }

      List<CatalogRow> rows = new ArrayList<>();
      CatalogRepository repository = new JpaCatalogRepository(entityManager);
      List<WorkPriceRange> prices = repository.priceRanges(vehicle.getVariant().getPriceClass());

      for (WorkPriceRange price : prices) {
        WorkDefinition work = price.getWork();

        if (selectedCategory != null && work.getCatalogCategory() != selectedCategory) {
          continue;
        }

        if (!search.isEmpty() && !work.getName().toLowerCase().contains(search)) {
          continue;
        }

        rows.add(
            new CatalogRow(
                work.getName(),
                work.getCatalogCategory(),
                work.getCategory(),
                price.getMinPrice(),
                price.getMaxPrice(),
                work.getIntervalKm(),
                work.getIntervalMonths()));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }
}
