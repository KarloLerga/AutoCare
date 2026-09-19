package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/** Jedan JFrame s CardLayoutom za prijavu, registraciju i aplikaciju. */
public final class MainFrame extends JFrame {
  public final LoginView login = new LoginView();
  public final OnboardingView onboarding = new OnboardingView();
  public final DashboardView dashboard = new DashboardView();
  public final VehiclesView vehicles = new VehiclesView();
  public final MaintenanceView maintenance = new MaintenanceView();
  public final CatalogView catalog = new CatalogView();
  public final ServicesView services = new ServicesView();
  public final ProblemsView problems = new ProblemsView();
  public final ProfileView profile = new ProfileView();
  public final Map<String, JButton> navigation = new LinkedHashMap<>();

  private final CardLayout roots = new CardLayout();
  private final CardLayout pages = new CardLayout();
  private final JPanel root = new JPanel(roots);
  private final JPanel content = new JPanel(pages);
  private final JLabel vehicleName = new JLabel("AutoCare");
  private final JLabel vehicleDetails = new JLabel(" ");
  private String page = "Dashboard";

  public MainFrame() {
    super("AutoCare");
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(1000, 680));
    setSize(1240, 820);
    setLocationRelativeTo(null);

    root.add(login, "LOGIN");
    root.add(onboarding, "REGISTER");

    JPanel shell = new JPanel(new BorderLayout());
    JPanel sidebar = Ui.column();
    sidebar.setOpaque(true);
    sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));
    sidebar.setPreferredSize(new Dimension(220, 700));

    JLabel vehicleIcon = new JLabel(icon(FontAwesomeSolid.CAR, 38));
    sidebar.add(vehicleIcon);
    sidebar.add(Box.createVerticalStrut(12));
    vehicleName.setFont(vehicleName.getFont().deriveFont(Font.BOLD, 16f));
    sidebar.add(vehicleName);
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(vehicleDetails);
    sidebar.add(Box.createVerticalStrut(24));

    addNavigation(sidebar, "Dashboard", dashboard, FontAwesomeSolid.TACHOMETER_ALT);
    addNavigation(sidebar, "Vozila", vehicles, FontAwesomeSolid.CAR);
    addNavigation(sidebar, "Održavanje", maintenance, FontAwesomeSolid.WRENCH);
    addNavigation(sidebar, "Katalog", catalog, FontAwesomeSolid.EURO_SIGN);
    addNavigation(sidebar, "Servisi", services, FontAwesomeSolid.CLIPBOARD);
    addNavigation(sidebar, "Bilješke", problems, FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    addNavigation(sidebar, "Profil", profile, FontAwesomeSolid.USER);

    sidebar.add(Box.createVerticalGlue());
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    shell.add(sidebar, BorderLayout.WEST);
    shell.add(content, BorderLayout.CENTER);
    root.add(shell, "APP");
    setContentPane(root);
  }

  private void addNavigation(
      JPanel sidebar, String name, JPanel panel, FontAwesomeSolid iconCode) {
    JButton navigationButton = Ui.button(name);
    navigationButton.setIcon(icon(iconCode, 17));
    navigationButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    navigationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    sidebar.add(navigationButton);
    sidebar.add(Box.createVerticalStrut(8));
    navigation.put(name, navigationButton);
    content.add(panel, name);
  }

  private FontIcon icon(FontAwesomeSolid iconCode, int size) {
    Color color = UIManager.getColor("Label.foreground");
    if (color == null) {
      color = Color.WHITE;
    }
    return FontIcon.of(iconCode, size, color);
  }

  public void auth() {
    roots.show(root, "LOGIN");
    getRootPane().setDefaultButton(login.login);
  }

  public void registration() {
    roots.show(root, "REGISTER");
    getRootPane().setDefaultButton(onboarding.next);
  }

  public void application() {
    roots.show(root, "APP");
    getRootPane().setDefaultButton(null);
  }

  public void showPage(String name) {
    page = name;
    pages.show(content, name);
    for (Map.Entry<String, JButton> entry : navigation.entrySet()) {
      int style = Font.PLAIN;
      if (entry.getKey().equals(name)) {
        style = Font.BOLD;
      }
      entry.getValue().setFont(entry.getValue().getFont().deriveFont(style));
    }
  }

  public String page() {
    return page;
  }

  public void context(VehicleRow vehicle) {
    vehicleName.setText(vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel());
    vehicleDetails.setText(
        vehicle.getYear() + " / " + Ui.km(vehicle.getMileage()) + " / " + Ui.date(LocalDate.now()));
    vehicleName.setToolTipText(
        vehicle.getVariant().getGeneration() + " / " + vehicle.getVariant().getEngine());
  }
}
