package hr.unizd.autocare.app;

import com.formdev.flatlaf.FlatLightLaf;
import hr.unizd.autocare.controller.MainController;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.persistence.JpaTransactionRunner;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.PasswordHasher;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.TransactionRunner;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.strategy.KeywordDiagnosticStrategy;
import hr.unizd.autocare.view.MainFrame;
import jakarta.persistence.EntityManagerFactory;
import java.awt.Font;
import java.time.Clock;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Ulaz normalne GUI aplikacije; nema naredbi za shemu, seed ili slike. */
public final class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  private Main() {}

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          initializeLookAndFeel();
          connectAndStart();
        });
  }

  private static void initializeLookAndFeel() {
    FlatLightLaf.setup();
    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    UIManager.put("Component.arc", 10);
    UIManager.put("Button.arc", 10);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("Table.rowHeight", 32);
  }

  private static void connectAndStart() {
    JFrame loadingFrame = new JFrame("AutoCare - povezivanje");
    loadingFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    loadingFrame.add(new JLabel("Povezivanje s bazom...", SwingConstants.CENTER));
    loadingFrame.setSize(560, 130);
    loadingFrame.setLocationRelativeTo(null);
    loadingFrame.setVisible(true);

    SwingWorker<EntityManagerFactory, Void> worker =
        new SwingWorker<>() {
          @Override
          protected EntityManagerFactory doInBackground() {
            return DatabaseConfig.open();
          }

          @Override
          protected void done() {
            EntityManagerFactory entityManagerFactory = null;

            try {
              entityManagerFactory = get();
              loadingFrame.dispose();
              showApplication(entityManagerFactory);
            } catch (InterruptedException exception) {
              Thread.currentThread().interrupt();
              loadingFrame.dispose();
              showStartupError(exception);
              closeAndExit(entityManagerFactory, 1);
            } catch (ExecutionException | RuntimeException exception) {
              loadingFrame.dispose();
              showStartupError(exception);
              closeAndExit(entityManagerFactory, 1);
            }
          }
        };

    worker.execute();
  }

  private static void showApplication(EntityManagerFactory entityManagerFactory) {
    Clock clock = Clock.systemDefaultZone();
    TransactionRunner transactions = new JpaTransactionRunner(entityManagerFactory);
    PasswordHasher passwordHasher = new PasswordHasher();

    AuthService authService = new AuthService(transactions, passwordHasher, clock);
    CatalogService catalogService = new CatalogService(transactions);
    VehicleService vehicleService = new VehicleService(transactions, clock);
    ServiceRecordService serviceService = new ServiceRecordService(transactions, clock);
    MaintenanceService maintenanceService = new MaintenanceService(transactions, clock);
    ProblemService problemService =
        new ProblemService(transactions, new KeywordDiagnosticStrategy(), clock);
    DashboardService dashboardService = new DashboardService(transactions, clock);

    MainFrame frame = new MainFrame();
    Session session = new Session();
    AppEvents events = new AppEvents();
    Runnable shutdown = () -> closeAndExit(entityManagerFactory, 0);

    new MainController(
        frame,
        session,
        authService,
        catalogService,
        vehicleService,
        serviceService,
        maintenanceService,
        problemService,
        dashboardService,
        events,
        shutdown);

    frame.setVisible(true);
  }

  private static void closeAndExit(EntityManagerFactory entityManagerFactory, int exitCode) {
    if (entityManagerFactory == null) {
      System.exit(exitCode);
      return;
    }

    new Thread(
            () -> {
              try {
                entityManagerFactory.close();
              } catch (RuntimeException exception) {
                LOGGER.warning("Zatvaranje: " + exception.getClass().getSimpleName());
              } finally {
                System.exit(exitCode);
              }
            },
            "autocare-close")
        .start();
  }

  private static void showStartupError(Exception exception) {
    // Raw SQL exceptions may contain connection or query details.
    LOGGER.warning("Pokretanje nije uspjelo: " + exception.getClass().getSimpleName());
    JOptionPane.showMessageDialog(
        null,
        "Povezivanje nije uspjelo. Provjerite mrezu i vanjsku konfiguraciju baze.\n"
            + "Aplikacija nije mijenjala strukturu baze.",
        "AutoCare",
        JOptionPane.ERROR_MESSAGE);
  }
}
