package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
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

/** Čitanje kataloga; svi radovi dolaze iz konkretnih pravila odabrane varijante. */
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

  public List<WorkRow> works(long ownerId, long vehicleId, WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      return workRows(new JpaCatalogRepository(entityManager), vehicle.getVariant().getId(), category);
    } finally {
      entityManager.close();
    }
  }

  public List<WorkRow> onboardingWorks(long variantId, WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository repository = new JpaCatalogRepository(entityManager);
      if (repository.findVariant(variantId) == null) {
        throw new AppException("Odaberite postojeću varijantu vozila.");
      }
      return workRows(repository, variantId, category);
    } finally {
      entityManager.close();
    }
  }

  static List<WorkRow> workRows(
      CatalogRepository catalogRepository, long variantId, WorkCategory category) {
    List<WorkRow> rows = new ArrayList<>();
    for (VehicleWorkRule rule : catalogRepository.rules(variantId)) {
      if (rule.getWork().getCategory() == category) {
        rows.add(
            new WorkRow(
                rule.getWork().getId(),
                rule.getWork().getCode(),
                rule.getWork().getName(),
                rule.getWork().getCategory()));
      }
    }
    return rows;
  }
}
