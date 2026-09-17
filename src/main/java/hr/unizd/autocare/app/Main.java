package hr.unizd.autocare.app;

import com.formdev.flatlaf.FlatDarkLaf;
import hr.unizd.autocare.controller.MainController;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.strategy.KeywordDiagnosticStrategy;
import hr.unizd.autocare.view.MainFrame;
import jakarta.persistence.EntityManagerFactory;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ulaz normalne GUI aplikacije. */
public final class Main {
  private Main() {}

  public static void main(String[] arguments) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            startApplication();
          }
        });
  }

  private static void startApplication() {
    initializeLookAndFeel();
    EntityManagerFactory entityManagerFactory = null;

    try {
      entityManagerFactory = DatabaseConfig.open();

      AuthService authService = new AuthService(entityManagerFactory);
      CatalogService catalogService = new CatalogService(entityManagerFactory);
      VehicleService vehicleService = new VehicleService(entityManagerFactory);
      ServiceRecordService serviceRecordService =
          new ServiceRecordService(entityManagerFactory);
      MaintenanceService maintenanceService =
          new MaintenanceService(entityManagerFactory);
      ProblemService problemService =
          new ProblemService(entityManagerFactory, new KeywordDiagnosticStrategy());
      DashboardService dashboardService = new DashboardService(entityManagerFactory);

      MainFrame frame = new MainFrame();
      Session session = new Session();
      AppEvents events = new AppEvents();

      new MainController(
          frame,
          session,
          authService,
          catalogService,
          vehicleService,
          serviceRecordService,
          maintenanceService,
          problemService,
          dashboardService,
          events,
          entityManagerFactory);

      frame.setVisible(true);
    } catch (RuntimeException exception) {
      if (entityManagerFactory != null) {
        entityManagerFactory.close();
      }

      JOptionPane.showMessageDialog(
          null,
          "Povezivanje nije uspjelo. Provjerite mrezu i vanjsku konfiguraciju baze.",
          "AutoCare",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private static void initializeLookAndFeel() {
    FlatDarkLaf.setup();
    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    UIManager.put("Component.arc", 10);
    UIManager.put("Button.arc", 10);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("Table.rowHeight", 32);
  }
}
