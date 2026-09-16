package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.DiagnosticRule;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaCatalogRepository implements CatalogRepository {
  private final EntityManager em;

  public JpaCatalogRepository(EntityManager em) {
    this.em = em;
  }

  public List<String> makes(int y) {
    return em.createQuery(
            "select distinct v.make from VehicleVariant v where v.yearFrom<=:y and (v.yearTo is"
                + " null or v.yearTo>=:y) order by v.make",
            String.class)
        .setParameter("y", y)
        .getResultList();
  }

  public List<String> models(int y, String make) {
    return em.createQuery(
            "select distinct v.model from VehicleVariant v where v.make=:m and v.yearFrom<=:y and"
                + " (v.yearTo is null or v.yearTo>=:y) order by v.model",
            String.class)
        .setParameter("m", make)
        .setParameter("y", y)
        .getResultList();
  }

  public List<VehicleVariant> variants(int y, String make, String model, String search) {
    String q = (search == null ? "" : search).strip().toLowerCase(Locale.ROOT);
    return em.createQuery(
            "select v from VehicleVariant v where v.make=:m and v.model=:model and v.yearFrom<=:y"
                + " and (v.yearTo is null or v.yearTo>=:y) and (locate(:q,lower(v.generation))>0 or"
                + " locate(:q,lower(v.engineLabel))>0 or :q='') order by"
                + " v.generation,v.engineLabel,v.id",
            VehicleVariant.class)
        .setParameter("m", make)
        .setParameter("model", model)
        .setParameter("y", y)
        .setParameter("q", q)
        .setMaxResults(201)
        .getResultList();
  }

  public VehicleVariant variant(long id) {
    VehicleVariant v = em.find(VehicleVariant.class, id);
    if (v == null) {
      throw new AppException(AppException.Kind.NOT_FOUND, "Odaberite postojecu varijantu vozila.");
    }
    return v;
  }

  public List<WorkDefinition> works(WorkCategory category) {
    return em.createQuery(
            "select w from WorkDefinition w where w.category=:c order by w.name",
            WorkDefinition.class)
        .setParameter("c", category)
        .getResultList();
  }

  public WorkDefinition work(long id) {
    WorkDefinition w = em.find(WorkDefinition.class, id);
    if (w == null) {
      throw new AppException(AppException.Kind.NOT_FOUND, "Rad nije pronadjen.");
    }
    return w;
  }

  public List<VehicleWorkRule> rules(long variant) {
    return em.createQuery(
            "select r from VehicleWorkRule r join fetch r.work where r.variant.id=:v order by"
                + " r.work.name",
            VehicleWorkRule.class)
        .setParameter("v", variant)
        .getResultList();
  }

  public List<DiagnosticRule> diagnosticRules() {
    return em.createQuery(
            "select r from DiagnosticRule r join fetch r.candidate where r.active=true order by"
                + " r.candidate.id,r.id",
            DiagnosticRule.class)
        .getResultList();
  }
}
