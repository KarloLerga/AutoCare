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

  static Account account(AppUser user) {
    return new Account(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getActiveVehicle() == null ? null : user.getActiveVehicle().getId());
  }

  static VariantRow variant(VehicleVariant variant) {
    return new VariantRow(
        variant.getId(),
        variant.getCode(),
        variant.getMake(),
        variant.getModel(),
        variant.getGeneration(),
        variant.getEngineLabel(),
        variant.getFuelType(),
        variant.getTransmission(),
        variant.getPowerHp(),
        variant.getYearFrom(),
        variant.getYearTo(),
        variant.getImagePath());
  }

  static VehicleRow vehicle(Vehicle vehicle, Long activeVehicleId) {
    return new VehicleRow(
        vehicle.getId(),
        variant(vehicle.getVariant()),
        vehicle.getProductionYear(),
        vehicle.getCurrentMileage(),
        Objects.equals(vehicle.getId(), activeVehicleId));
  }

  static ServiceRow service(ServiceRecord serviceRecord) {
    StringJoiner names = new StringJoiner(", ");
    for (ServiceItem serviceItem : serviceRecord.getItems()) {
      names.add(serviceItem.getWork().getName());
    }
    return new ServiceRow(
        serviceRecord.getId(),
        serviceRecord.getServiceDate(),
        serviceRecord.getMileage(),
        names.toString(),
        serviceRecord.total(),
        serviceRecord.getNote());
  }

  static ProblemRow problem(Problem problem) {
    return new ProblemRow(
        problem.getId(),
        problem.getDescription(),
        problem.getStatus(),
        problem.getCreatedAt(),
        problem.getSuggestedRepair() == null ? null : problem.getSuggestedRepair().getName(),
        problem.getMatchPercent(),
        problem.getEstimatedCost(),
        problem.getEstimateNote(),
        problem.getResolvedByService() == null ? null : problem.getResolvedByService().getId());
  }

}
