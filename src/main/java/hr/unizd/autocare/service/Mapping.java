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

/** Pretvara domenske objekte u podatke koje prikazuje GUI. */
final class Mapping {
  private Mapping() {}

  static Account account(AppUser user) {
    return new Account(user.getId(), user.getName(), user.getEmail());
  }

  static VariantRow variant(VehicleVariant variant) {
    return new VariantRow(
        variant.getId(),
        variant.getMake(),
        variant.getModel(),
        variant.getGeneration(),
        variant.getEngineLabel(),
        variant.getFuelType(),
        variant.getTransmission(),
        variant.getPowerHp());
  }

  static VehicleRow vehicle(Vehicle vehicle, Long activeVehicleId) {
    boolean active = false;
    if (activeVehicleId != null && vehicle.getId().equals(activeVehicleId)) {
      active = true;
    }

    return new VehicleRow(
        vehicle.getId(),
        variant(vehicle.getVariant()),
        vehicle.getProductionYear(),
        vehicle.getCurrentMileage(),
        active);
  }

  static ServiceRow service(ServiceRecord serviceRecord) {
    StringBuilder names = new StringBuilder();

    for (ServiceItem serviceItem : serviceRecord.getItems()) {
      if (names.length() > 0) {
        names.append(", ");
      }
      names.append(serviceItem.getWork().getName());
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
        problem.getCategory(),
        problem.getStatus(),
        problem.getCreatedAt());
  }
}
