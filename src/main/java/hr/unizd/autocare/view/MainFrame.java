package hr.unizd.autocare.view;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
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

public final class MainFrame extends JFrame {
  public final LoginView login = new LoginView();
  public final RegistrationView registration = new RegistrationView();
  public final DashboardView dashboard = new DashboardView();
  public final VehiclesView vehicles = new VehiclesView();
  public final MaintenanceView maintenance = new MaintenanceView();
  public final CatalogView catalog = new CatalogView();
  public final ServicesView services = new ServicesView();
  public final ProblemsView problems = new ProblemsView();

  public final JButton dashboardButton = new JButton("Dashboard");
  public final JButton vehiclesButton = new JButton("Vozila");
  public final JButton maintenanceButton = new JButton("Održavanje");
  public final JButton catalogButton = new JButton("Katalog");
  public final JButton servicesButton = new JButton("Servisi");
  public final JButton problemsButton = new JButton("Problemi");
  public final JButton logoutButton = new JButton("Odjava");

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
    root.add(registration, "REGISTER");

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

    addNavigation(sidebar, dashboardButton, dashboard, FontAwesomeSolid.TACHOMETER_ALT);
    addNavigation(sidebar, vehiclesButton, vehicles, FontAwesomeSolid.CAR);
    addNavigation(sidebar, maintenanceButton, maintenance, FontAwesomeSolid.WRENCH);
    addNavigation(sidebar, catalogButton, catalog, FontAwesomeSolid.EURO_SIGN);
    addNavigation(sidebar, servicesButton, services, FontAwesomeSolid.CLIPBOARD);
    addNavigation(sidebar, problemsButton, problems, FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    sidebar.add(Box.createVerticalGlue());
    logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    sidebar.add(logoutButton);
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    shell.add(sidebar, BorderLayout.WEST);
    shell.add(content, BorderLayout.CENTER);
    root.add(shell, "APP");
    setContentPane(root);
  }

  private void addNavigation(
      JPanel sidebar, JButton button, JPanel panel, FontAwesomeSolid iconCode) {
    button.setIcon(icon(iconCode, 17));
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    sidebar.add(button);
    sidebar.add(Box.createVerticalStrut(8));
    content.add(panel, button.getText());
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
    getRootPane().setDefaultButton(registration.finish);
  }

  public void application() {
    roots.show(root, "APP");
    getRootPane().setDefaultButton(null);
  }

  public void showPage(String name) {
    page = name;
    pages.show(content, name);

    setSelected(dashboardButton, name.equals("Dashboard"));
    setSelected(vehiclesButton, name.equals("Vozila"));
    setSelected(maintenanceButton, name.equals("Održavanje"));
    setSelected(catalogButton, name.equals("Katalog"));
    setSelected(servicesButton, name.equals("Servisi"));
    setSelected(problemsButton, name.equals("Problemi"));
  }

  private void setSelected(JButton button, boolean selected) {
    int style = Font.PLAIN;
    if (selected) {
      style = Font.BOLD;
    }
    button.setFont(button.getFont().deriveFont(style));
  }

  public String page() {
    return page;
  }

  public void context(Vehicle vehicle) {
    boolean hasVehicle = vehicle != null;
    dashboardButton.setEnabled(hasVehicle);
    maintenanceButton.setEnabled(hasVehicle);
    catalogButton.setEnabled(true);
    servicesButton.setEnabled(hasVehicle);
    problemsButton.setEnabled(hasVehicle);

    if (!hasVehicle) {
      vehicleName.setText("Nema dodanog vozila");
      vehicleDetails.setText("Dodajte vozilo u izborniku Vozila");
      vehicleName.setToolTipText(null);
      return;
    }

    vehicleName.setText(vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel());
    vehicleDetails.setText(
        vehicle.getProductionYear() + " / " + Ui.km(vehicle.getCurrentMileage()) + " / " + Ui.date(LocalDate.now()));
    vehicleName.setToolTipText(
        vehicle.getVariant().getGeneration() + " / " + vehicle.getVariant().getEngineLabel());
  }
}
