package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.DiagnosticRule;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.Analysis;
import hr.unizd.autocare.model.Data.DiagnosticResult;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.RuleData;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.repository.Repositories;
import hr.unizd.autocare.strategy.DiagnosticStrategy;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Orkestracija dijagnostike; bodovanje delegira strategiji. */
public final class ProblemService {
  private final TransactionRunner tx;
  private final DiagnosticStrategy strategy;
  private final Clock clock;

  public ProblemService(TransactionRunner tx, DiagnosticStrategy strategy, Clock clock) {
    this.tx = tx;
    this.strategy = strategy;
    this.clock = clock;
  }

  public List<ProblemRow> list(long owner, long vehicle, ProblemStatus status) {
    return tx.read(
        r -> {
          r.vehicles().requireOwned(owner, vehicle);
          List<ProblemRow> rows = new ArrayList<>();
          for (Problem p : r.problems().list(owner, vehicle, status)) {
            rows.add(Mapping.problem(p));
          }
          return List.copyOf(rows);
        });
  }

  /**
   * Cita pregledana pravila i delegira cistoj strategiji; ne sprema Problem.
   *
   * @param owner vlasnik vozila
   * @param vehicle vlastito vozilo cija varijanta odredjuje pokrivenost
   * @param description slobodan opis do2000 znakova
   * @return rangirani kandidati; prazna lista je valjan rezultat, ne potvrda ispravnosti vozila
   */
  public Analysis analyze(long owner, long vehicle, String description) {
    String clean = Checks.text(description, 2000, "Opis simptoma");
    return tx.read(
        r -> {
          Vehicle v = r.vehicles().requireOwned(owner, vehicle);
          return analysis(r, v, clean);
        });
  }

  private Analysis analysis(Repositories r, Vehicle v, String description) {
    Set<Long> supported = new HashSet<>();
    for (VehicleWorkRule rule : r.catalog().rules(v.getVariant().getId())) {
      if (rule.getWork().getCategory() == WorkCategory.REPAIR) {
        supported.add(rule.getWork().getId());
      }
    }
    Map<Long, WorkRow> prices = new HashMap<>();
    for (WorkRow w : CatalogService.workRows(r, v.getVariant().getId(), WorkCategory.REPAIR)) {
      prices.put(w.getId(), w);
    }
    List<RuleData> rules = new ArrayList<>();
    for (DiagnosticRule rule : r.catalog().diagnosticRules()) {
      WorkRow w = prices.get(rule.getCandidate().getId());
      if (w != null && supported.contains(w.getId())) {
        rules.add(
            new RuleData(
                w.getId(),
                w.getName(),
                rule.getPhrase(),
                rule.getWeight(),
                w.getPrice(),
                w.getPriceNote()));
      }
    }
    return new Analysis(description, strategy.analyze(description, rules));
  }

  /**
   * Sprema najbolji informativni rezultat tek nakon ponovne provjere previewa.
   *
   * @param owner vlasnik
   * @param vehicle ID vozila
   * @param requestKey jedinstveni UUID zadrzan tijekom ponovnog pokusaja
   * @param preview rezultat prikazan korisniku, ne proizvoljno povjerenje podacima GUI-a
   * @return ID spremljenog problema
   */
  public long save(long owner, long vehicle, String requestKey, Analysis preview) {
    if (preview == null) {
      throw AppException.validation("Prvo analizirajte opis.");
    }
    return tx.write(
        r -> {
          r.users().require(owner);
          Optional<Problem> old = r.problems().byRequest(owner, requestKey);
          if (old.isPresent()) {
            if (!old.get().getVehicle().getId().equals(vehicle)) {
              throw AppException.conflict("Zahtjev pripada drugom vozilu.");
            }
            if (!old.get()
                .getDescription()
                .equals(Checks.text(preview.getDescription(), 2000, "Opis simptoma"))) {
              throw AppException.conflict("Isti zahtjev vec ima spremljen drugi opis.");
            }
            return old.get().getId();
          }
          Vehicle v = r.vehicles().requireOwned(owner, vehicle);
          Analysis fresh =
              analysis(r, v, Checks.text(preview.getDescription(), 2000, "Opis simptoma"));
          if (!same(preview, fresh)) {
            throw AppException.conflict("Pravila ili cijene su se promijenili. Ponovite analizu.");
          }
          DiagnosticResult top = fresh.getResults().isEmpty() ? null : fresh.getResults().get(0);
          Problem p =
              new Problem(
                  v,
                  requestKey,
                  fresh.getDescription(),
                  LocalDateTime.now(clock),
                  top == null ? null : r.catalog().work(top.getCandidateId()),
                  top == null ? null : top.getScore(),
                  top == null ? null : top.getPrice(),
                  top == null ? null : top.getPriceNote());
          r.problems().add(p);
          return p.getId();
        });
  }

  private boolean same(Analysis a, Analysis b) {
    if (a.getResults().size() != b.getResults().size()) {
      return false;
    }
    for (int i = 0; i < a.getResults().size(); i++) {
      DiagnosticResult x = a.getResults().get(i), y = b.getResults().get(i);
      if (x.getCandidateId() != y.getCandidateId()
          || x.getScore().compareTo(y.getScore()) != 0
          || !Objects.equals(x.getPrice(), y.getPrice())
          || !Objects.equals(x.getPriceNote(), y.getPriceNote())) {
        return false;
      }
    }
    return true;
  }

  public Long findSaved(long owner, String key) {
    return tx.read(r -> r.problems().byRequest(owner, key).map(Problem::getId).orElse(null));
  }
}
