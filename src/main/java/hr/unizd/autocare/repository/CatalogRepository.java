package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.VehiclePriceClass;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.domain.WorkPriceRange;
import java.util.List;

/** Dohvat kataloga vozila, zahvata i cijena. */
public interface CatalogRepository {
  List<String> makes();

  List<String> models(String make);

  List<Integer> years(String make, String model);

  List<VehicleVariant> variants(String make, String model, int year);

  VehicleVariant findVariant(long id);

  List<WorkDefinition> works(WorkCategory category);

  WorkDefinition findWork(long id);

  List<WorkPriceRange> priceRanges(VehiclePriceClass priceClass);
}
