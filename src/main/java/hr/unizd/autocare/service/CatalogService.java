package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.domain.WorkPriceRange;
import hr.unizd.autocare.model.Data.CatalogRow;
import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

  public List<VariantRow> variants(String make, String model, int year) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository repository = new JpaCatalogRepository(entityManager);
      List<VariantRow> rows = new ArrayList<>();
      for (VehicleVariant variant : repository.variants(make, model, year)) {
        rows.add(Mapping.variant(variant));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public List<WorkRow> works(WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return workRows(new JpaCatalogRepository(entityManager).works(category));
    } finally {
      entityManager.close();
    }
  }

  public List<WorkRow> works(long ownerId, long vehicleId, WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      if (vehicleRepository.findForOwner(ownerId, vehicleId) == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      return workRows(new JpaCatalogRepository(entityManager).works(category));
    } finally {
      entityManager.close();
    }
  }

  public List<WorkRow> onboardingWorks(long variantId, WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository repository = new JpaCatalogRepository(entityManager);
      if (repository.findVariant(variantId) == null) {
        throw new IllegalArgumentException("Odaberite postojeću varijantu vozila.");
      }
      return workRows(repository.works(category));
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
        search = searchText.trim().toLowerCase(Locale.ROOT);
      }

      List<CatalogRow> rows = new ArrayList<>();
      CatalogRepository repository = new JpaCatalogRepository(entityManager);
      List<WorkPriceRange> prices = repository.priceRanges(vehicle.getVariant().getPriceClass());

      for (WorkPriceRange price : prices) {
        WorkDefinition work = price.getWork();

        if (selectedCategory != null && work.getCatalogCategory() != selectedCategory) {
          continue;
        }

        if (!search.isEmpty()) {
          String name = work.getName().toLowerCase(Locale.ROOT);
          String code = work.getCode().toLowerCase(Locale.ROOT);
          if (!name.contains(search) && !code.contains(search)) {
            continue;
          }
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

  private static List<WorkRow> workRows(List<WorkDefinition> works) {
    List<WorkRow> rows = new ArrayList<>();
    for (WorkDefinition work : works) {
      rows.add(new WorkRow(work.getId(), work.getName(), work.getCategory()));
    }
    return rows;
  }
}
