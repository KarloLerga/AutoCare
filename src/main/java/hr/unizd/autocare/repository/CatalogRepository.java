package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import java.util.List;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface CatalogRepository {
  List<String> makes();

  List<String> models(String make);

  List<Integer> years(String make, String model);

  List<VehicleVariant> variants(String make, String model, int year);

  VehicleVariant findVariant(long id);

  List<WorkDefinition> works(WorkCategory category);

  WorkDefinition findWork(long id);

  VehicleWorkRule findRule(long variantId, long workId);

  List<VehicleWorkRule> rules(long variantId);
}
