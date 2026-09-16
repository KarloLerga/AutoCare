package hr.unizd.autocare.repository;
import hr.unizd.autocare.domain.*;
import java.util.*;
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
