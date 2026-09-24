package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.model.Data.VehicleRow;
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

public final class MainController implements Observer {
  private final MainFrame frame;
  private final Session session;
  private final VehicleService vehicleService;
  private final DashboardService dashboardService;
  private final VehiclesController vehicles;
  private final CatalogController catalog;
  private final ServicesController services;
  private final MaintenanceController maintenance;
  private final ProblemsController problems;

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
    services =
        new ServicesController(
            frame, serviceRecordService, catalogService, problemService, session, subject);
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

  private void activateForm() {
    frame.dashboardButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Dashboard");
          }
        });

    frame.vehiclesButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Vozila");
          }
        });

    frame.maintenanceButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Održavanje");
          }
        });

    frame.catalogButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Katalog");
          }
        });

    frame.servicesButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Servisi");
          }
        });

    frame.problemsButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            navigate("Problemi");
          }
        });

    frame.logoutButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            logout();
          }
        });
  }

  private void enter(int ownerId) {
    session.login(ownerId);
    refreshContext(true);
  }

  private void refreshContext(boolean showDashboard) {
    if (session.getOwnerId() == 0) {
      return;
    }

    try {
      VehicleRow activeVehicle = vehicleService.active(session.getOwnerId());
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

  private void navigate(String pageName) {
    frame.showPage(pageName);
    loadVisible();
  }

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

  private void loadDashboard() {
    try {
      frame.dashboard.showDashboard(
          dashboardService.get(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.dashboard, exception);
    }
  }

  @Override
  public void update(AppEvent event) {
    if (event == AppEvent.ACTIVE_VEHICLE_CHANGED) {
      refreshContext(true);
    } else {
      refreshContext(false);
    }
  }

  private void logout() {
    session.logout();
    frame.login.password.setText("");
    frame.vehicles.setRows(new ArrayList<>());
    frame.services.setRows(new ArrayList<>());
    frame.maintenance.setRows(new ArrayList<>());
    frame.catalog.search.setText("");
    frame.catalog.category.setSelectedIndex(0);
    frame.problems.setRows(new ArrayList<>());
    frame.auth();
  }
}
