package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.DiagnosticRule;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaCatalogRepository implements CatalogRepository {
  private final EntityManager entityManager;

  public JpaCatalogRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<String> makes(int year) {
    return entityManager
        .createQuery(
            "select distinct variant.make from VehicleVariant variant "
                + "where variant.yearFrom<=:year and "
                + "(variant.yearTo is null or variant.yearTo>=:year) order by variant.make",
            String.class)
        .setParameter("year", year)
        .getResultList();
  }

  @Override
  public List<String> models(int year, String make) {
    return entityManager
        .createQuery(
            "select distinct variant.model from VehicleVariant variant "
                + "where variant.make=:make and variant.yearFrom<=:year and "
                + "(variant.yearTo is null or variant.yearTo>=:year) order by variant.model",
            String.class)
        .setParameter("make", make)
        .setParameter("year", year)
        .getResultList();
  }

  @Override
  public List<VehicleVariant> variants(int year, String make, String model, String search) {
    String searchText = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
    return entityManager
        .createQuery(
            "select variant from VehicleVariant variant where variant.make=:make "
                + "and variant.model=:model and variant.yearFrom<=:year "
                + "and (variant.yearTo is null or variant.yearTo>=:year) "
                + "and (locate(:searchText,lower(variant.generation))>0 "
                + "or locate(:searchText,lower(variant.engineLabel))>0 or :searchText='') "
                + "order by variant.generation,variant.engineLabel,variant.id",
            VehicleVariant.class)
        .setParameter("make", make)
        .setParameter("model", model)
        .setParameter("year", year)
        .setParameter("searchText", searchText)
        .setMaxResults(201)
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
  public List<VehicleWorkRule> rules(long variantId) {
    return entityManager
        .createQuery(
            "select rule from VehicleWorkRule rule join fetch rule.work "
                + "where rule.variant.id=:variantId order by rule.work.name",
            VehicleWorkRule.class)
        .setParameter("variantId", variantId)
        .getResultList();
  }

  @Override
  public List<DiagnosticRule> diagnosticRules() {
    return entityManager
        .createQuery(
            "select diagnosticRule from DiagnosticRule diagnosticRule "
                + "join fetch diagnosticRule.candidate where diagnosticRule.active=true "
                + "order by diagnosticRule.candidate.id,diagnosticRule.id",
            DiagnosticRule.class)
        .getResultList();
  }
}
