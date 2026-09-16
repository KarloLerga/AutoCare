package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.VehicleRow;
import java.util.Objects;
import java.util.StringJoiner;

/** Mapira tocno potrebne podatke dok je persistence context otvoren. */
final class Mapping {
  private Mapping() {}

  static Account account(AppUser u) {
    return new Account(
        u.getId(),
        u.getVersion(),
        u.getName(),
        u.getEmail(),
        u.getActiveVehicle() == null ? null : u.getActiveVehicle().getId());
  }

  static VariantRow variant(VehicleVariant v) {
    return new VariantRow(
        v.getId(),
        v.getCode(),
        v.getMake(),
        v.getModel(),
        v.getGeneration(),
        v.getEngineLabel(),
        v.getFuelType(),
        v.getTransmission(),
        v.getPowerHp(),
        v.getYearFrom(),
        v.getYearTo(),
        v.getImagePath());
  }

  static VehicleRow vehicle(Vehicle v, Long active) {
    return new VehicleRow(
        v.getId(),
        v.getVersion(),
        variant(v.getVariant()),
        v.getProductionYear(),
        v.getCurrentMileage(),
        Objects.equals(v.getId(), active));
  }

  static ServiceRow service(ServiceRecord s) {
    StringJoiner names = new StringJoiner(", ");
    for (ServiceItem i : s.getItems()) {
      names.add(i.getWork().getName());
    }
    return new ServiceRow(
        s.getId(), s.getServiceDate(), s.getMileage(), names.toString(), s.total(), s.getNote());
  }

  static ProblemRow problem(Problem p) {
    return new ProblemRow(
        p.getId(),
        p.getVersion(),
        p.getDescription(),
        p.getStatus(),
        p.getCreatedAt(),
        p.getSuggestedRepair() == null ? null : p.getSuggestedRepair().getName(),
        p.getMatchPercent(),
        p.getEstimatedCost(),
        p.getEstimateNote(),
        p.getResolvedByService() == null ? null : p.getResolvedByService().getId());
  }

  static void version(long actual, long expected) {
    if (actual != expected) {
      throw AppException.conflict("Podaci su u medjuvremenu promijenjeni. Osvjezite obrazac.");
    }
  }
}
