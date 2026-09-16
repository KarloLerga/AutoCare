package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.TransactionRunner;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Upravljanje korisnikovim vozilima i aktivnim vozilom. */
public final class VehicleService {

    private final TransactionRunner transactions;
    private final Clock clock;

    public VehicleService(TransactionRunner transactions, Clock clock) {
        this.transactions = transactions;
        this.clock = clock;
    }

    public List<VehicleRow> list(long ownerId) {
        return transactions.read(repositories -> {
            AppUser user = repositories.users().require(ownerId);

            Long activeId = user.getActiveVehicle() == null
                    ? null
                    : user.getActiveVehicle().getId();

            List<VehicleRow> rows = new ArrayList<>();

            for (Vehicle vehicle : repositories.vehicles().list(ownerId)) {
                rows.add(Mapping.vehicle(vehicle, activeId));
            }

            return List.copyOf(rows);
        });
    }

    public VehicleRow active(long ownerId) {
        return transactions.read(repositories -> {
            AppUser user = repositories.users().require(ownerId);

            if (user.getActiveVehicle() == null) {
                throw AppException.conflict(
                        "Korisnik nema aktivno vozilo.");
            }

            Vehicle vehicle = repositories.vehicles().requireOwned(
                    ownerId,
                    user.getActiveVehicle().getId());

            return Mapping.vehicle(vehicle, vehicle.getId());
        });
    }

    public long add(long ownerId, VehicleInput input) {
        validate(input);

        return transactions.write(repositories -> {
            AppUser user = repositories.users().require(ownerId);

            Vehicle vehicle = new Vehicle(
                    user,
                    repositories.catalog().variant(input.getVariantId()),
                    input.getYear(),
                    input.getMileage());

            repositories.vehicles().add(vehicle);

            return vehicle.getId();
        });
    }

    public void update(
            long ownerId,
            long vehicleId,
            VehicleInput input) {

        validate(input);

        transactions.write(repositories -> {
            Vehicle vehicle =
                    repositories.vehicles().requireOwned(ownerId, vehicleId);

            boolean identityChanged =
                    !vehicle.getVariant().getId().equals(input.getVariantId())
                            || vehicle.getProductionYear() != input.getYear();

            if (identityChanged
                    && repositories.vehicles().hasHistory(vehicleId)) {
                throw AppException.conflict(
                        "Vozilu koje vec ima povijest nije moguce promijeniti model.");
            }

            if (identityChanged) {
                vehicle.changeIdentity(
                        repositories.catalog().variant(input.getVariantId()),
                        input.getYear());
            }

            vehicle.updateMileage(input.getMileage());

            return null;
        });
    }

    public boolean identityEditable(long ownerId, long vehicleId) {
        return transactions.read(repositories -> {
            repositories.vehicles().requireOwned(ownerId, vehicleId);
            return !repositories.vehicles().hasHistory(vehicleId);
        });
    }

    public void activate(long ownerId, long vehicleId) {
        transactions.write(repositories -> {
            AppUser user = repositories.users().require(ownerId);
            Vehicle vehicle =
                    repositories.vehicles().requireOwned(ownerId, vehicleId);

            user.activate(vehicle);

            return null;
        });
    }

    public void delete(long ownerId, long vehicleId) {
        transactions.write(repositories -> {
            AppUser user = repositories.users().require(ownerId);
            List<Vehicle> vehicles = repositories.vehicles().list(ownerId);

            if (vehicles.size() <= 1) {
                throw AppException.conflict(
                        "Posljednje vozilo nije moguce obrisati.");
            }

            Vehicle vehicle =
                    repositories.vehicles().requireOwned(ownerId, vehicleId);

            if (user.getActiveVehicle() != null
                    && user.getActiveVehicle().getId().equals(vehicleId)) {

                for (Vehicle other : vehicles) {
                    if (!other.getId().equals(vehicleId)) {
                        user.activate(other);
                        break;
                    }
                }
            }

            repositories.problems().deleteForVehicle(vehicleId);
            repositories.services().deleteForVehicle(vehicleId);
            repositories.vehicles().delete(vehicle);

            return null;
        });
    }

    private void validate(VehicleInput input) {
        if (input.getYear() < 1886
                || input.getYear() > LocalDate.now(clock).getYear()) {
            throw AppException.validation(
                    "Nevaljana godina proizvodnje.");
        }

        Checks.mileage(input.getMileage());
    }
}
