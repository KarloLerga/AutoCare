package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.DiagnosticRule;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import java.util.List;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface CatalogRepository {
  List<String> makes(int year);

  List<String> models(int year, String make);

  List<VehicleVariant> variants(int year, String make, String model, String search);

  VehicleVariant variant(long id);

  List<WorkDefinition> works(WorkCategory category);

  WorkDefinition work(long id);

  List<VehicleWorkRule> rules(long variant);

  List<DiagnosticRule> diagnosticRules();
}
