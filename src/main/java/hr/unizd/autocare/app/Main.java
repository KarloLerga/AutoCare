package hr.unizd.autocare.app;

import com.formdev.flatlaf.FlatDarkLaf;
import hr.unizd.autocare.controller.MainController;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import jakarta.persistence.EntityManagerFactory;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ulazna točka AutoCare desktop aplikacije.
 *
 * <p>Klasa pokreće Swing sučelje na Event Dispatch Threadu, inicijalizira izgled aplikacije,
 * otvara zajednički {@link jakarta.persistence.EntityManagerFactory}, stvara glavne Service
 * objekte i povezuje ih s glavnim prozorom, sesijom, Observer infrastrukturom i
 * {@code MainControllerom}. Na ovom mjestu se ručno sastavlja aplikacija bez dodatnog
 * dependency injection frameworka.
 */
public class Main {
  /** Sprječava stvaranje instance jer klasa služi samo za pokretanje aplikacije. */
  private Main() {}

  /**
   * Predaje pokretanje Swing aplikacije na Event Dispatch Thread.
   *
   * @param arguments argumenti naredbenog retka; aplikacija ih ne koristi
   */
  public static void main(String[] arguments) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        startApplication();
      }
    });
  }

  /**
   * Sastavlja i prikazuje cijelu aplikaciju.
   *
   * <p>Metoda postavlja izgled, otvara vezu prema persistence sloju, stvara Service objekte,
   * glavni prozor, zajedničku sesiju i Subject te ih predaje {@code MainControlleru}. Ako
   * inicijalizacija baze ne uspije, zatvara eventualno otvoreni {@code EntityManagerFactory}
   * i korisniku prikazuje razumljivu poruku o pogrešci.
   */
  private static void startApplication() {
    initializeLookAndFeel();
    EntityManagerFactory entityManagerFactory = null;

    try {
      entityManagerFactory = DatabaseConfig.open();

      AuthService authService = new AuthService(entityManagerFactory);
      CatalogService catalogService = new CatalogService(entityManagerFactory);
      VehicleService vehicleService = new VehicleService(entityManagerFactory);
      ServiceRecordService serviceRecordService = new ServiceRecordService(entityManagerFactory);
      MaintenanceService maintenanceService = new MaintenanceService(entityManagerFactory);
      ProblemService problemService = new ProblemService(entityManagerFactory);
      DashboardService dashboardService = new DashboardService(entityManagerFactory);

      MainFrame frame = new MainFrame();
      Session session = Session.getInstance();
      Subject subject = new Subject();

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
          subject);

      closeDatabaseWhenWindowCloses(frame, entityManagerFactory);
      frame.setVisible(true);
    } catch (RuntimeException exception) {
      if (entityManagerFactory != null) {
        entityManagerFactory.close();
      }

      JOptionPane.showMessageDialog(
          null,
          "Povezivanje s bazom nije uspjelo. Provjerite mrežu i podatke za povezivanje.",
          "AutoCare",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Registrira zatvaranje persistence resursa zajedno s glavnim prozorom aplikacije.
   *
   * @param frame glavni Swing prozor
   * @param entityManagerFactory zajednička JPA tvornica koju treba zatvoriti pri izlasku
   */
  private static void closeDatabaseWhenWindowCloses(MainFrame frame, EntityManagerFactory entityManagerFactory) {
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        frame.dispose();
        entityManagerFactory.close();
      }
    });
  }

  /**
   * Postavlja FlatLaf temu i zajedničke Swing vrijednosti izgleda.
   *
   * <p>Na jednom mjestu definira osnovni font, zaobljenja komponenti i visinu redaka tablica
   * kako bi svi ekrani imali dosljedan izgled.
   */
  private static void initializeLookAndFeel() {
    FlatDarkLaf.setup();
    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    UIManager.put("Component.arc", 10);
    UIManager.put("Button.arc", 10);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("Table.rowHeight", 32);
  }
}
