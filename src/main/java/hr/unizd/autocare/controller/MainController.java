package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.event.AppListener;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import jakarta.persistence.EntityManagerFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JButton;

/** Glavna navigacija, session i ponovno učitavanje aktivnog vozila. */
public final class MainController implements AppListener {
  private final MainFrame frame;
  private final Session session;
  private final VehicleService vehicleService;
  private final DashboardService dashboardService;
  private final AppEvents events;
  private final EntityManagerFactory entityManagerFactory;
  private final VehiclesController vehicles;
  private final ServicesController services;
  private final MaintenanceController maintenance;
  private final ProblemsController problems;
  private final ProfileController profile;

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
      AppEvents events,
      EntityManagerFactory entityManagerFactory) {
    this.frame = frame;
    this.session = session;
    this.vehicleService = vehicleService;
    this.dashboardService = dashboardService;
    this.events = events;
    this.entityManagerFactory = entityManagerFactory;

    vehicles = new VehiclesController(frame, vehicleService, catalogService, session, events);
    services =
        new ServicesController(
            frame, serviceRecordService, catalogService, problemService, session, events);
    maintenance = new MaintenanceController(frame, maintenanceService, catalogService, session);
    problems = new ProblemsController(frame, problemService, catalogService, session, events);
    profile = new ProfileController(frame, authService, session, events);

    new AuthController(
        frame,
        authService,
        catalogService,
        new AuthController.LoginListener() {
          @Override
          public void loggedIn(long ownerId) {
            enter(ownerId);
          }
        });
    activateForm();
    events.add(this);
    frame.auth();
  }

  private void activateForm() {
    for (Map.Entry<String, JButton> entry : frame.navigation.entrySet()) {
      final String pageName = entry.getKey();
      entry.getValue().addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
              navigate(pageName);
            }
          });
    }
    frame.profile.logout.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            logout();
          }
        });
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent event) {
            close();
          }
        });
  }

  private void enter(long ownerId) {
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
      if (showDashboard) {
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
    if (session.getActiveVehicle() == null) {
      return;
    }
    String page = frame.page();
    if (page.equals("Dashboard")) {
      loadDashboard();
    } else if (page.equals("Vozila")) {
      vehicles.load();
    } else if (page.equals("Servisi")) {
      services.load();
    } else if (page.equals("Održavanje")) {
      maintenance.load();
    } else if (page.equals("Problemi")) {
      problems.load();
    } else if (page.equals("Profil")) {
      profile.load();
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
  public void onChange(AppEvent event) {
    if (event == AppEvent.ACTIVE_VEHICLE_CHANGED) {
      refreshContext(true);
    } else if (event == AppEvent.VEHICLE_CHANGED || event == AppEvent.SERVICE_SAVED) {
      refreshContext(false);
    } else {
      loadVisible();
    }
  }

  private void logout() {
    session.logout();
    profile.clear();
    frame.login.password.setText("");
    frame.vehicles.setRows(new ArrayList<>());
    frame.services.setRows(new ArrayList<>());
    frame.maintenance.setRows(new ArrayList<>());
    frame.maintenance.setAvailableWorks(new ArrayList<>());
    frame.problems.setRows(new ArrayList<>());
    frame.auth();
  }

  private void close() {
    events.remove(this);
    session.logout();
    frame.dispose();
    entityManagerFactory.close();
  }
}
