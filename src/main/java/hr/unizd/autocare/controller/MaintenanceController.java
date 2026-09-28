package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;

/**
 * Upravlja prikazom izračunatih intervala održavanja aktivnog vozila.
 *
 * <p>Controller uzima identitet korisnika i vozila iz Sessiona, delegira izračun Serviceu i
 * predaje dobivene retke MaintenanceViewu.
 */
public class MaintenanceController {
  /** Glavni prozor koji sadrži prikaz održavanja i cilj za prikaz pogrešaka. */
  private final MainFrame frame;

  /** Dohvaća povijest i računa preostale servisne intervale. */
  private final MaintenanceService maintenanceService;

  /** Daje vlasnika i aktivno vozilo za koje treba učitati održavanje. */
  private final Session session;

  /**
   * Povezuje MaintenanceView s MaintenanceServiceom i korisničkom sesijom.
   *
   * @param frame glavni prozor
   * @param maintenanceService servis koji računa stanje održavanja
   * @param session zajednički korisnički kontekst
   */
  public MaintenanceController(MainFrame frame, MaintenanceService maintenanceService, Session session) {
    this.frame = frame;
    this.maintenanceService = maintenanceService;
    this.session = session;
  }

  /** Dohvaća održavanja za aktivno vozilo i prikazuje ih u MaintenanceViewu. */
  public void load() {
    try {
      frame.maintenance.setRows(maintenanceService.list(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.maintenance, exception);
    }
  }
}
