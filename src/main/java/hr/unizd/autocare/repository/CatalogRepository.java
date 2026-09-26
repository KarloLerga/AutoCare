package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** JPA dohvat kataloga vozila i standardnih zahvata. */
public class CatalogRepository {
  private final EntityManager entityManager;

  public CatalogRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public List<String> makes() {
    return entityManager
        .createQuery(
            "select distinct variant.make from VehicleVariant variant order by variant.make",
            String.class)
        .getResultList();
  }

  public List<String> models(String make) {
    return entityManager
        .createQuery(
            "select distinct variant.model from VehicleVariant variant "
                + "where variant.make=:make order by variant.model",
            String.class)
        .setParameter("make", make)
        .getResultList();
  }

  public List<Integer> years(String make, String model) {
    List<VehicleVariant> variants = entityManager
            .createQuery(
                "select variant from VehicleVariant variant where variant.make=:make "
                    + "and variant.model=:model order by variant.yearFrom,variant.id",
                VehicleVariant.class)
            .setParameter("make", make)
            .setParameter("model", model)
            .getResultList();

    List<Integer> years = new ArrayList<>();
    int currentYear = LocalDate.now().getYear();

    for (VehicleVariant variant : variants) {
      int lastYear = currentYear;
      if (variant.getYearTo() != null && variant.getYearTo() < currentYear) {
        lastYear = variant.getYearTo();
      }

      for (int year = variant.getYearFrom(); year <= lastYear; year++) {
        if (!years.contains(year)) {
          years.add(year);
        }
      }
    }

    Collections.sort(years);
    return years;
  }

  public List<VehicleVariant> variants(String make, String model, int year) {
    return entityManager
        .createQuery(
            "select variant from VehicleVariant variant where variant.make=:make "
                + "and variant.model=:model and variant.yearFrom<=:year "
                + "and (variant.yearTo is null or variant.yearTo>=:year) "
                + "order by variant.generation,variant.engineLabel,variant.id",
            VehicleVariant.class)
        .setParameter("make", make)
        .setParameter("model", model)
        .setParameter("year", year)
        .getResultList();
  }

  public VehicleVariant findVariant(int id) {
    return entityManager.find(VehicleVariant.class, id);
  }

  public List<WorkDefinition> works(WorkCategory category) {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work where work.category=:category order by work.name",
            WorkDefinition.class)
        .setParameter("category", category)
        .getResultList();
  }

  public List<WorkDefinition> allWorks() {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work order by work.catalogCategory,work.name",
            WorkDefinition.class)
        .getResultList();
  }

  public WorkDefinition findWork(int id) {
    return entityManager.find(WorkDefinition.class, id);
  }
}
