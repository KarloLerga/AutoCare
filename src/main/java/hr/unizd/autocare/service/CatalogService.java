package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.CatalogRow;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

/** Čitanje kataloga vozila i standardnih zahvata. */
public final class CatalogService {
  private final EntityManagerFactory entityManagerFactory;

  public CatalogService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<String> makes() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).makes();
    } finally {
      entityManager.close();
    }
  }

  public List<String> models(String make) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).models(make);
    } finally {
      entityManager.close();
    }
  }

  public List<Integer> years(String make, String model) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).years(make, model);
    } finally {
      entityManager.close();
    }
  }

  public List<VehicleVariant> variants(String make, String model, int year) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).variants(make, model, year);
    } finally {
      entityManager.close();
    }
  }

  public List<WorkDefinition> works(WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).works(category);
    } finally {
      entityManager.close();
    }
  }

  public List<CatalogRow> catalog() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      List<CatalogRow> rows = new ArrayList<>();
      for (WorkDefinition work : new CatalogRepository(entityManager).allWorks()) {
        rows.add(
            new CatalogRow(
                work.getName(),
                work.getCatalogCategory(),
                work.getCategory(),
                work.getMinPrice(),
                work.getMaxPrice(),
                work.getIntervalKm(),
                work.getIntervalMonths()));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }
}
