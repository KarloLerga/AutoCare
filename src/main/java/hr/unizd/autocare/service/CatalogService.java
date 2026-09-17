package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Citanje kataloga vozila i radova za GUI. */
public final class CatalogService {
  private final EntityManagerFactory entityManagerFactory;

  public CatalogService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<String> makes(int year) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      return catalogRepository.makes(year);
    } finally {
      entityManager.close();
    }
  }

  public List<String> models(int year, String make) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      return catalogRepository.models(year, make);
    } finally {
      entityManager.close();
    }
  }

  public List<VariantRow> variants(int year, String make, String model, String search) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      List<VariantRow> rows = new ArrayList<>();
      for (VehicleVariant variant : catalogRepository.variants(year, make, model, search)) {
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
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      return workRows(catalogRepository, vehicle.getVariant().getId(), category);
    } finally {
      entityManager.close();
    }
  }

  public List<WorkRow> onboardingWorks(long variantId, WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      if (catalogRepository.findVariant(variantId) == null) {
        throw new AppException("Odaberite postojecu varijantu vozila.");
      }
      return workRows(catalogRepository, variantId, category);
    } finally {
      entityManager.close();
    }
  }

  static List<WorkRow> workRows(
      CatalogRepository catalogRepository, long variantId, WorkCategory category) {
    Map<Long, VehicleWorkRule> rules = new HashMap<>();
    for (VehicleWorkRule rule : catalogRepository.rules(variantId)) {
      rules.put(rule.getWork().getId(), rule);
    }

    List<WorkRow> rows = new ArrayList<>();
    for (WorkDefinition work : catalogRepository.works(category)) {
      VehicleWorkRule rule = rules.get(work.getId());
      if (!appliesWithoutSpecificRule(work, rule)) {
        continue;
      }

      BigDecimal price;
      String priceNote;
      if (rule != null) {
        price = rule.getEstimatedPrice();
        priceNote = rule.getEstimateNote();
      } else {
        price = work.getDefaultEstimatedPrice();
        priceNote = work.getEstimateNote();
      }
      rows.add(
          new WorkRow(
              work.getId(),
              work.getCode(),
              work.getName(),
              work.getCategory(),
              price,
              priceNote));
    }
    return rows;
  }

  private static boolean appliesWithoutSpecificRule(
      WorkDefinition work, VehicleWorkRule rule) {
    if (rule != null) {
      return true;
    }
    if (work.getCode().startsWith("OTHER_")) {
      return true;
    }
    return work.getDefaultIntervalKm() != null
        || work.getDefaultIntervalMonths() != null
        || work.getDefaultEstimatedPrice() != null;
  }
}
