package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.model.Data.VehicleRow;

/** Pretvara domenske objekte u podatke koje prikazuje GUI. */
final class Mapping {
  private Mapping() {}

  static Account account(AppUser user) {
    return new Account(user.getId(), user.getName(), user.getEmail());
  }

  static VehicleRow vehicle(Vehicle vehicle, Long activeVehicleId) {
    boolean active = false;
    if (activeVehicleId != null && vehicle.getId().equals(activeVehicleId)) {
      active = true;
    }

    return new VehicleRow(
        vehicle.getId(),
        vehicle.getVariant(),
        vehicle.getProductionYear(),
        vehicle.getCurrentMileage(),
        active);
  }

  static ServiceRow service(ServiceRecord serviceRecord) {
    String names = "";

    for (ServiceItem serviceItem : serviceRecord.getItems()) {
      if (!names.isEmpty()) {
        names += ", ";
      }
      names += serviceItem.getWork().getName();
    }

    return new ServiceRow(
        serviceRecord.getId(),
        serviceRecord.getServiceDate(),
        serviceRecord.getMileage(),
        names,
        serviceRecord.total(),
        serviceRecord.getNote());
  }
}
