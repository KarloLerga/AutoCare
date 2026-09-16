package hr.unizd.autocare.app;
import com.formdev.flatlaf.FlatLightLaf;
import jakarta.persistence.*;
import hr.unizd.autocare.controller.MainController;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.persistence.JpaTransactionRunner;
import hr.unizd.autocare.service.*;
import hr.unizd.autocare.strategy.KeywordDiagnosticStrategy;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import javax.swing.*;
import java.awt.*;
import java.time.*;
/** Composition root. CLI za shemu/seed je odvojen od normalnog GUI pokretanja. */
public final class Main {
    private Main() {
    }
    public static void main(String[] args) {
        if(args.length>0) {
            hr.unizd.autocare.tools.DatabaseTool.run(args);
            return;
        }
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.rowHeight", 32);
        SwingUtilities.invokeLater(()-> {
            JFrame loading=new JFrame("AutoCare - povezivanje");
            loading.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            loading.add(new JLabel("Povezivanje s bazom i provjera sheme...", SwingConstants.CENTER));
            loading.setSize(560, 130);
            loading.setLocationRelativeTo(null);
            loading.setVisible(true);
            new SwingWorker<EntityManagerFactory, Void>() {
                protected EntityManagerFactory doInBackground() {
                    return DatabaseConfig.open("validate", false);
                }
                protected void done() {
                    try {
                        EntityManagerFactory emf=get();
                        loading.dispose();
                        start(emf);
                    }
                    catch(Exception ex) {
                        loading.dispose();
                        Throwable cause=ex.getCause()==null?ex:ex.getCause();
                        JOptionPane.showMessageDialog(null, "Pokretanje nije uspjelo. Provjerite AUTOCARE_DB_* varijable, TLS i schema-update.\n"+cause.getMessage(), "AutoCare", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
            }
            .execute();
        });
    }
    private static void start(EntityManagerFactory emf) {
        Clock clock=Clock.systemDefaultZone();
        TransactionRunner tx=new JpaTransactionRunner(emf);
        PasswordHasher hasher=new PasswordHasher();
        AuthService auth=new AuthService(tx, hasher, clock);
        CatalogService catalog=new CatalogService(tx);
        VehicleService vehicles=new VehicleService(tx, clock);
        ServiceRecordService services=new ServiceRecordService(tx, clock);
        MaintenanceService maintenance=new MaintenanceService(tx, clock);
        ProblemService problems=new ProblemService(tx, new KeywordDiagnosticStrategy(), clock);
        DashboardService dashboard=new DashboardService(tx, clock);
        MainFrame frame=new MainFrame();
        Session session=new Session();
        AppEvents events=new AppEvents();
        Runnable shutdown=()->new Thread(()-> {
            try {
                emf.close();
            }
            finally {
                System.exit(0);
            }
        }, "autocare-close").start();
        new MainController(frame, session, auth, catalog, vehicles, services, maintenance, problems, dashboard, events, shutdown);
        frame.setVisible(true);
    }
}
