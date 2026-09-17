package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.DiagnosticRule;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.Analysis;
import hr.unizd.autocare.model.Data.DiagnosticResult;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.RuleData;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaUserRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.UserRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import hr.unizd.autocare.strategy.DiagnosticStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Orkestracija dijagnostike; bodovanje delegira strategiji. */
public final class ProblemService {
  private final EntityManagerFactory entityManagerFactory;
  private final DiagnosticStrategy strategy;

  public ProblemService(EntityManagerFactory entityManagerFactory, DiagnosticStrategy strategy) {
    this.entityManagerFactory = entityManagerFactory;
    this.strategy = strategy;
  }

  public List<ProblemRow> list(long ownerId, long vehicleId, ProblemStatus status) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);
      if (vehicleRepository.findForOwner(ownerId, vehicleId) == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      List<ProblemRow> rows = new ArrayList<>();
      for (Problem problem : problemRepository.list(ownerId, vehicleId, status)) {
        rows.add(Mapping.problem(problem));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public Analysis analyze(long ownerId, long vehicleId, String description) {
    String cleanDescription = Checks.text(description, 2000, "Opis simptoma");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      return analysis(catalogRepository, vehicle, cleanDescription);
    } finally {
      entityManager.close();
    }
  }

  private Analysis analysis(
      CatalogRepository catalogRepository, Vehicle vehicle, String description) {
    Set<Long> supportedWorkIds = new HashSet<>();
    for (VehicleWorkRule rule : catalogRepository.rules(vehicle.getVariant().getId())) {
      if (rule.getWork().getCategory() == WorkCategory.REPAIR) {
        supportedWorkIds.add(rule.getWork().getId());
      }
    }

    Map<Long, WorkRow> prices = new HashMap<>();
    for (WorkRow workRow :
        CatalogService.workRows(catalogRepository, vehicle.getVariant().getId(), WorkCategory.REPAIR)) {
      prices.put(workRow.getId(), workRow);
    }

    List<RuleData> rules = new ArrayList<>();
    for (DiagnosticRule diagnosticRule : catalogRepository.diagnosticRules()) {
      WorkRow workRow = prices.get(diagnosticRule.getCandidate().getId());
      if (workRow != null && supportedWorkIds.contains(workRow.getId())) {
        rules.add(
            new RuleData(
                workRow.getId(),
                workRow.getName(),
                diagnosticRule.getPhrase(),
                diagnosticRule.getWeight(),
                workRow.getPrice(),
                workRow.getPriceNote()));
      }
    }
    return new Analysis(description, strategy.analyze(description, rules));
  }

  /** Sprema rezultat koji je korisnik upravo vidio; nema retry/idempotency sloj. */
  public long save(long ownerId, long vehicleId, Analysis preview) {
    if (preview == null) {
      throw new AppException("Prvo analizirajte opis.");
    }

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);
      if (userRepository.findById(ownerId) == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }

      DiagnosticResult topResult = null;
      if (!preview.getResults().isEmpty()) {
        topResult = preview.getResults().get(0);
      }
      WorkDefinition suggestedWork = findSuggestedWork(catalogRepository, topResult);
      Problem problem =
          new Problem(
              vehicle,
              Checks.text(preview.getDescription(), 2000, "Opis simptoma"),
              LocalDateTime.now(),
              suggestedWork,
              topResult == null ? null : topResult.getScore(),
              topResult == null ? null : topResult.getPrice(),
              topResult == null ? null : topResult.getPriceNote());
      problemRepository.add(problem);
      transaction.commit();
      return problem.getId();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  private WorkDefinition findSuggestedWork(
      CatalogRepository catalogRepository, DiagnosticResult topResult) {
    if (topResult == null) {
      return null;
    }
    WorkDefinition suggestedWork = catalogRepository.findWork(topResult.getCandidateId());
    if (suggestedWork == null) {
      throw new AppException("Predlozeni rad vise nije dostupan.");
    }
    return suggestedWork;
  }
}
