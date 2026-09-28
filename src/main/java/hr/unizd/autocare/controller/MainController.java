package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Observer;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Središnji Controller za navigaciju i zajednički kontekst AutoCare aplikacije.
 *
 * <p>Klasa stvara funkcionalne Controllere, povezuje navigacijske gumbe, reagira na prijavu,
 * učitava aktivno vozilo i određuje koji ekran treba osvježiti. Implementira Observer kako bi
 * nakon promjene vozila ili spremanja servisa mogao osvježiti zajednički aplikacijski kontekst.
 */
public class MainController implements Observer {
  /** Glavni Swing prozor za navigaciju, prikaz konteksta i pristup ekranima. */
  private final MainFrame frame;

  /** Prijavljenog korisnika i trenutačno aktivno vozilo dijeli s ostalim Controllerima. */
  private final Session session;

  /** Dohvaća i mijenja vozila pri prijavi i promjeni aktivnog vozila. */
  private final VehicleService vehicleService;

  /** Priprema prikazne podatke Dashboarda za aktivno vozilo. */
  private final DashboardService dashboardService;

  /** Controller za popis, dodavanje, aktiviranje i uređivanje vozila. */
  private final VehiclesController vehicles;

  /** Controller informativnog kataloga radova. */
  private final CatalogController catalog;

  /** Controller servisne povijesti aktivnog vozila. */
  private final ServicesController services;

  /** Controller izračunatih intervala održavanja. */
  private final MaintenanceController maintenance;

  /** Controller evidencije problema aktivnog vozila. */
  private final ProblemsController problems;

  /**
   * Povezuje glavni prozor sa svim Service objektima i Controllerima aplikacije.
   *
   * <p>Konstruktor stvara funkcionalne Controllere, konfigurira AuthController callback,
   * registrira navigacijske listenere i prijavljuje MainController kao Observer na zajednički
   * Subject. Po završetku prikazuje ekran za prijavu.
   *
   * @param frame glavni prozor aplikacije
   * @param session zajednička korisnička sesija
   * @param authService servis prijave i registracije
   * @param catalogService servis kataloga
   * @param vehicleService servis vozila
   * @param serviceRecordService servis za servisnu povijest
   * @param maintenanceService servis održavanja
   * @param problemService servis problema
   * @param dashboardService servis Dashboard podataka
   * @param subject Subject koji objavljuje aplikacijske događaje
   */
  public MainController(
      MainFrame frame,
      Session session,
      AuthService authService,
      CatalogService catalogService,
      VehicleService vehicleService,
      ServiceRecordService serviceRecordService,
      MaintenanceService maintenanceService,
      ProblemService problemService,
      DashboardService dashboardService,
      Subject subject) {
    this.frame = frame;
    this.session = session;
    this.vehicleService = vehicleService;
    this.dashboardService = dashboardService;

    vehicles = new VehiclesController(frame, vehicleService, catalogService, session, subject);
    services = new ServicesController(frame, serviceRecordService, catalogService, problemService, session, subject);
    maintenance = new MaintenanceController(frame, maintenanceService, session);
    catalog = new CatalogController(frame, catalogService);
    problems = new ProblemsController(frame, problemService, session);

    new AuthController(
        frame,
        authService,
        new AuthController.LoginListener() {
          @Override
          public void loggedIn(int ownerId) {
            enter(ownerId);
          }
        });

    activateForm();
    subject.addObserver(this);
    frame.auth();
  }

  /**
   * Povezuje gumbe glavne navigacije i odjave s odgovarajućim akcijama Controllera.
   *
   * <p>Svaki listener samo delegira na imenovanu metodu kako bi logika navigacije ostala izdvojena
   * iz anonimnih Swing listenera.
   */
  private void activateForm() {
    frame.dashboardButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Dashboard");
      }
    });

    frame.vehiclesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Vozila");
      }
    });

    frame.maintenanceButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Održavanje");
      }
    });

    frame.catalogButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Katalog");
      }
    });

    frame.servicesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Servisi");
      }
    });

    frame.problemsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        navigate("Problemi");
      }
    });

    frame.logoutButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        logout();
      }
    });
  }

  /**
   * Otvara aplikacijski dio nakon uspješne prijave.
   *
   * @param ownerId identifikator prijavljenog korisnika
   */
  private void enter(int ownerId) {
    session.login(ownerId);
    refreshContext(true);
  }

  /**
   * Ponovno učitava korisnikovo aktivno vozilo i sinkronizira zajedničko stanje aplikacije.
   *
   * <p>Ako nitko nije prijavljen, metoda ne pokreće dohvat. Inače dohvaća aktivno vozilo kroz
   * {@link VehicleService}, ažurira {@link Session} i zaglavlje prozora te prikazuje aplikacijski
   * dio sučelja. Ako vozilo nije odabrano, otvara stranicu Vozila; ako je pozivatelj zatražio
   * Dashboard, otvara njega. Na kraju učitava podatke stranice koja je tada vidljiva. Runtime
   * pogreške prikazuje zajedničkim {@link Ui#error} dijalogom.
   *
   * @param showDashboard treba li nakon osvježavanja aktivnog vozila otvoriti Dashboard
   */
  private void refreshContext(boolean showDashboard) {
    if (session.getOwnerId() == 0) {
      return;
    }

    try {
      Vehicle activeVehicle = vehicleService.active(session.getOwnerId());
      session.setActiveVehicle(activeVehicle);
      frame.context(activeVehicle);
      frame.application();

      if (activeVehicle == null) {
        frame.showPage("Vozila");
      } else if (showDashboard) {
        frame.showPage("Dashboard");
      }

      loadVisible();
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  /**
   * Mijenja aktivnu aplikacijsku stranicu i odmah učitava njezin aktualni sadržaj.
   *
   * @param pageName naziv stranice registriran u MainFrame CardLayoutu
   */
  private void navigate(String pageName) {
    frame.showPage(pageName);
    loadVisible();
  }

  /**
   * Učitava podatke samo za stranicu koju trenutačno prikazuje {@link MainFrame}.
   *
   * <p>Stranice Vozila i Katalog učitavaju se neovisno o aktivnom vozilu. Za Dashboard, Servise,
   * Održavanje i Probleme najprije zahtijeva da ga {@link Session} sadrži; zatim delegira dohvat
   * odgovarajućem Controlleru. Tako se ne šalju upiti koji ovise o vozilu dok korisnik još nije
   * odabrao kontekst.
   */
  private void loadVisible() {
    String page = frame.page();
    if (page.equals("Vozila")) {
      vehicles.load();
      return;
    }
    if (page.equals("Katalog")) {
      catalog.load();
      return;
    }
    if (session.getActiveVehicle() == null) {
      return;
    }

    if (page.equals("Dashboard")) {
      loadDashboard();
    } else if (page.equals("Servisi")) {
      services.load();
    } else if (page.equals("Održavanje")) {
      maintenance.load();
    } else if (page.equals("Problemi")) {
      problems.load();
    }
  }

  /**
   * Dohvaća sažetak aktivnog vozila kroz {@link DashboardService} i predaje ga DashboardViewu.
   *
   * <p>Runtime pogreške dohvatnog toka prikazuje uz Dashboard, bez promjene trenutačne stranice.
   */
  private void loadDashboard() {
    try {
      frame.dashboard.showDashboard(dashboardService.get(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.dashboard, exception);
    }
  }

  /**
   * Reagira na događaj promjene koji je objavio {@link Subject}.
   *
   * <p>Promjena aktivnog vozila zahtijeva potpuno ponovno učitavanje konteksta i otvaranje
   * Dashboarda, dok ostale promjene osvježavaju aktivni kontekst i vidljive podatke bez prisilne
   * promjene stranice.
   *
   * @param event aplikacijski događaj koji je potrebno obraditi
   */
  @Override
  public void update(AppEvent event) {
    if (event == AppEvent.ACTIVE_VEHICLE_CHANGED) {
      refreshContext(true);
    } else {
      refreshContext(false);
    }
  }

  /**
   * Odjavljuje korisnika i vraća aplikaciju u čisto početno stanje.
   *
   * <p>Čisti Session, osjetljivo polje lozinke i prikazane podatke prethodnog korisnika prije
   * povratka na ekran prijave.
   */
  private void logout() {
    session.logout();
    frame.login.password.setText("");
    frame.vehicles.setRows(new ArrayList<Vehicle>(), null);
    frame.services.setRows(new ArrayList<>());
    frame.maintenance.setRows(new ArrayList<>());
    frame.catalog.search.setText("");
    frame.catalog.category.setSelectedIndex(0);
    frame.problems.setRows(new ArrayList<>());
    frame.auth();
  }
}
