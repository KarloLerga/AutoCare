package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Vlasnistvo, aktivno vozilo i pravilo najmanje jednog vozila. */
public final class VehicleService {
  private final TransactionRunner tx;
  private final Clock clock;

  public VehicleService(TransactionRunner tx, Clock clock) {
    this.tx = tx;
    this.clock = clock;
  }

  public List<VehicleRow> list(long owner) {
    return tx.read(
        r -> {
          AppUser u = r.users().require(owner);
          Long active = u.getActiveVehicle() == null ? null : u.getActiveVehicle().getId();
          List<VehicleRow> rows = new ArrayList<>();
          for (Vehicle v : r.vehicles().list(owner)) {
            rows.add(Mapping.vehicle(v, active));
          }
          return List.copyOf(rows);
        });
  }

  public VehicleRow active(long owner) {
    return tx.read(
        r -> {
          AppUser u = r.users().require(owner);
          if (u.getActiveVehicle() == null) {
            throw AppException.conflict("Racun nema aktivno vozilo. Provjerite integritet baze.");
          }
          return Mapping.vehicle(
              r.vehicles().requireOwned(owner, u.getActiveVehicle().getId()),
              u.getActiveVehicle().getId());
        });
  }

  public long add(long owner, VehicleInput input) {
    validateYear(input);
    return tx.write(
        r -> {
          AppUser u = r.users().require(owner);
          Vehicle v =
              new Vehicle(
                  u,
                  r.catalog().variant(input.getVariantId()),
                  input.getYear(),
                  input.getMileage());
          r.vehicles().add(v);
          return v.getId();
        });
  }

  public void update(long owner, long vehicle, long expected, VehicleInput input) {
    validateYear(input);
    tx.write(
        r -> {
          r.users().require(owner);
          Vehicle v = r.vehicles().requireOwned(owner, vehicle);
          Mapping.version(v.getVersion(), expected);
          boolean identityChanged =
              !v.getVariant().getId().equals(input.getVariantId())
                  || v.getProductionYear() != input.getYear();
          if (identityChanged && r.vehicles().hasHistory(vehicle)) {
            throw AppException.conflict("Identitet vozila s povijescu nije moguce promijeniti.");
          }
          if (identityChanged) {
            v.changeIdentity(r.catalog().variant(input.getVariantId()), input.getYear());
          }
          v.updateMileage(input.getMileage());
          return null;
        });
  }

  public boolean identityEditable(long owner, long vehicle) {
    return tx.read(
        r -> {
          r.vehicles().requireOwned(owner, vehicle);
          return !r.vehicles().hasHistory(vehicle);
        });
  }

  public void activate(long owner, long vehicle) {
    tx.write(
        r -> {
          AppUser u = r.users().require(owner);
          u.activate(r.vehicles().requireOwned(owner, vehicle));
          return null;
        });
  }

  /**
   * Brise vlastito vozilo i njegove zapise, nikad zajednicki katalog.
   *
   * @param owner vlasnik; njegov red koordinira konkurentne write operacije
   * @param vehicle vozilo za brisanje, ne smije biti posljednje
   * @param replacement drugo vlastito vozilo ako se brise aktivno; inace moze biti null
   */
  public void delete(long owner, long vehicle, Long replacement) {
    tx.write(
        r -> {
          AppUser u = r.users().require(owner);
          List<Vehicle> all = r.vehicles().list(owner);
          if (all.size() <= 1) {
            throw AppException.conflict("Posljednje vozilo nije moguce obrisati.");
          }
          Vehicle v = r.vehicles().requireOwned(owner, vehicle);
          if (u.getActiveVehicle() != null && u.getActiveVehicle().getId().equals(vehicle)) {
            if (replacement == null || replacement.equals(vehicle)) {
              throw AppException.validation("Odaberite drugo vlastito aktivno vozilo.");
            }
            u.activate(r.vehicles().requireOwned(owner, replacement));
          }
          // Bulk brisanje samo ovdje: djeca nisu prethodno ucitana u persistence context.
          r.problems().deleteForVehicle(vehicle);
          r.services().deleteForVehicle(vehicle);
          r.vehicles().delete(v);
          return null;
        });
  }

  private void validateYear(VehicleInput input) {
    if (input.getYear() < 1886 || input.getYear() > LocalDate.now(clock).getYear()) {
      throw AppException.validation("Nevaljana godina proizvodnje.");
    }
    Checks.mileage(input.getMileage());
  }
}
