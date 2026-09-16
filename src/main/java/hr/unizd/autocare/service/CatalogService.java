package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.repository.Repositories;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Ucitaj male izbornike i ograniceni rezultat trazenja, ne cijeli katalog za GUI. */
public final class CatalogService {
  private final TransactionRunner tx;

  public CatalogService(TransactionRunner tx) {
    this.tx = tx;
  }

  public List<String> makes(int year) {
    return tx.read(r -> r.catalog().makes(year));
  }

  public List<String> models(int year, String make) {
    return tx.read(r -> r.catalog().models(year, make));
  }

  public List<VariantRow> variants(int year, String make, String model, String search) {
    return tx.read(
        r -> {
          List<VariantRow> out = new ArrayList<>();
          for (VehicleVariant v : r.catalog().variants(year, make, model, search)) {
            out.add(Mapping.variant(v));
          }
          return List.copyOf(out);
        });
  }

  public List<WorkRow> works(long owner, long vehicle, WorkCategory category) {
    return tx.read(
        r -> {
          Vehicle v = r.vehicles().requireOwned(owner, vehicle);
          return workRows(r, v.getVariant().getId(), category);
        });
  }

  public List<WorkRow> onboardingWorks(long variant, WorkCategory category) {
    return tx.read(
        r -> {
          r.catalog().variant(variant);
          return workRows(r, variant, category);
        });
  }

  static List<WorkRow> workRows(Repositories r, long variant, WorkCategory category) {
    Map<Long, VehicleWorkRule> rules = new HashMap<>();
    for (VehicleWorkRule rule : r.catalog().rules(variant)) {
      rules.put(rule.getWork().getId(), rule);
    }
    List<WorkRow> out = new ArrayList<>();
    for (WorkDefinition w : r.catalog().works(category)) {
      VehicleWorkRule rule = rules.get(w.getId());
      if (rule == null && !w.getCode().startsWith("OTHER_")) {
        continue;
      }
      boolean specific = rule != null && rule.getEstimatedPrice() != null;
      out.add(
          new WorkRow(
              w.getId(),
              w.getCode(),
              w.getName(),
              w.getCategory(),
              specific ? rule.getEstimatedPrice() : null,
              rule != null ? rule.getEstimateNote() : w.getEstimateNote()));
    }
    return List.copyOf(out);
  }
}
