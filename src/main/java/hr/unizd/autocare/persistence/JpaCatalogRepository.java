package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** JPA upiti koriste vezane parametre i postojeći EntityManager. */
public final class JpaCatalogRepository implements CatalogRepository {
  private final EntityManager entityManager;

  public JpaCatalogRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<String> makes() {
    return entityManager
        .createQuery(
            "select distinct variant.make from VehicleVariant variant order by variant.make",
            String.class)
        .getResultList();
  }

  @Override
  public List<String> models(String make) {
    return entityManager
        .createQuery(
            "select distinct variant.model from VehicleVariant variant "
                + "where variant.make=:make order by variant.model",
            String.class)
        .setParameter("make", make)
        .getResultList();
  }

  @Override
  public List<Integer> years(String make, String model) {
    List<VehicleVariant> variants =
        entityManager
            .createQuery(
                "select variant from VehicleVariant variant where variant.make=:make "
                    + "and variant.model=:model order by variant.yearFrom,variant.id",
                VehicleVariant.class)
            .setParameter("make", make)
            .setParameter("model", model)
            .getResultList();
    TreeSet<Integer> years = new TreeSet<>();
    int currentYear = LocalDate.now().getYear();
    for (VehicleVariant variant : variants) {
      int to =
          variant.getYearTo() == null
              ? currentYear
              : Math.min(currentYear, variant.getYearTo());
      for (int year = variant.getYearFrom(); year <= to; year++) {
        years.add(year);
      }
    }
    return new ArrayList<>(years);
  }

  @Override
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

  @Override
  public VehicleVariant findVariant(long id) {
    return entityManager.find(VehicleVariant.class, id);
  }

  @Override
  public List<WorkDefinition> works(WorkCategory category) {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work where work.category=:category order by work.name",
            WorkDefinition.class)
        .setParameter("category", category)
        .getResultList();
  }

  @Override
  public WorkDefinition findWork(long id) {
    return entityManager.find(WorkDefinition.class, id);
  }

  @Override
  public VehicleWorkRule findRule(long variantId, long workId) {
    List<VehicleWorkRule> rules =
        entityManager
            .createQuery(
                "select rule from VehicleWorkRule rule join fetch rule.work "
                    + "where rule.variant.id=:variantId and rule.work.id=:workId",
                VehicleWorkRule.class)
            .setParameter("variantId", variantId)
            .setParameter("workId", workId)
            .setMaxResults(1)
            .getResultList();
    return rules.isEmpty() ? null : rules.get(0);
  }

  @Override
  public List<VehicleWorkRule> rules(long variantId) {
    return entityManager
        .createQuery(
            "select rule from VehicleWorkRule rule join fetch rule.work "
                + "where rule.variant.id=:variantId order by rule.work.category,rule.work.name",
            VehicleWorkRule.class)
        .setParameter("variantId", variantId)
        .getResultList();
  }
}
