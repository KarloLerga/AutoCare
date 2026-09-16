package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** Jedan JFrame i dva CardLayouta: auth i glavni sadrzaj. */
public final class MainFrame extends JFrame {
  public final LoginView login = new LoginView();
  public final DashboardView dashboard = new DashboardView();
  public final VehiclesView vehicles = new VehiclesView();
  public final MaintenanceView maintenance = new MaintenanceView();
  public final ServicesView services = new ServicesView();
  public final ProblemsView problems = new ProblemsView();
  public final ProfileView profile = new ProfileView();
  public final Map<String, JButton> navigation = new LinkedHashMap<>();
  public final JButton refresh = Ui.button("Osvjezi", false);
  public final JLabel status = Ui.hint("Spremno");
  private final CardLayout roots = new CardLayout(), pages = new CardLayout();
  private final JPanel root = new JPanel(roots), content = new JPanel(pages);
  private final JLabel vehicleName = new JLabel("AutoCare"), vehicleDetails = new JLabel(" ");
  private String page = "Dashboard";
  private final VehicleImage vehicleImage = new VehicleImage();

  public MainFrame() {
    super("AutoCare");
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(1000, 680));
    setSize(1240, 820);
    setLocationRelativeTo(null);
    root.add(login, "LOGIN");
    JPanel shell = new JPanel(new BorderLayout(0, 0));
    shell.setBackground(Ui.BACKGROUND);
    JPanel sidebar = Ui.column();
    sidebar.setOpaque(true);
    sidebar.setBackground(Color.WHITE);
    sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));
    sidebar.setPreferredSize(new Dimension(220, 700));
    vehicleName.setFont(vehicleName.getFont().deriveFont(Font.BOLD, 16f));
    sidebar.add(vehicleImage);
    sidebar.add(Box.createVerticalStrut(12));
    sidebar.add(vehicleName);
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(vehicleDetails);
    sidebar.add(Box.createVerticalStrut(24));
    String[] names = {"Dashboard", "Vozila", "Odrzavanje", "Servisi", "Problemi", "Profil"};
    JPanel[] panels = {dashboard, vehicles, maintenance, services, problems, profile};
    for (int i = 0; i < names.length; i++) {
      JButton b = Ui.button(names[i], false);
      b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
      b.setAlignmentX(Component.LEFT_ALIGNMENT);
      sidebar.add(b);
      sidebar.add(Box.createVerticalStrut(8));
      navigation.put(names[i], b);
      content.add(panels[i], names[i]);
    }
    sidebar.add(Box.createVerticalGlue());
    sidebar.add(Ui.hint("AutoCare / NOOP"));
    shell.add(sidebar, BorderLayout.WEST);
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    shell.add(content, BorderLayout.CENTER);
    shell.add(Ui.row(refresh, status), BorderLayout.SOUTH);
    root.add(shell, "APP");
    setContentPane(root);
  }

  public void auth() {
    roots.show(root, "LOGIN");
    getRootPane().setDefaultButton(login.login);
  }

  public void application() {
    roots.show(root, "APP");
    getRootPane().setDefaultButton(null);
  }

  public void showPage(String name) {
    page = name;
    pages.show(content, name);
    for (Map.Entry<String, JButton> e : navigation.entrySet()) {
      e.getValue().setBackground(e.getKey().equals(name) ? new Color(0xDFEDF3) : Color.WHITE);
    }
  }

  public String page() {
    return page;
  }

  public void context(VehicleRow v) {
    vehicleImage.showVehicle(v.getVariant().getImagePath(), v.getVariant().getFuel());
    vehicleName.setText(v.getVariant().getMake() + " " + v.getVariant().getModel());
    vehicleDetails.setText(v.getYear() + " / " + Ui.km(v.getMileage()));
    vehicleName.setToolTipText(v.getVariant().getGeneration() + " / " + v.getVariant().getEngine());
  }
}
