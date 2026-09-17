package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.view.components.Ui;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

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

  private final CardLayout roots = new CardLayout();
  private final CardLayout pages = new CardLayout();
  private final JPanel root = new JPanel(roots);
  private final JPanel content = new JPanel(pages);
  private final JLabel vehicleIcon = new JLabel();
  private final JLabel vehicleName = new JLabel("AutoCare");
  private final JLabel vehicleDetails = new JLabel(" ");
  private String page = "Dashboard";

  public MainFrame() {
    super("AutoCare");

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(1000, 680));
    setSize(1240, 820);
    setLocationRelativeTo(null);

    refresh.setIcon(icon(FontAwesomeSolid.SYNC_ALT, 15));

    vehicleIcon.setIcon(icon(FontAwesomeSolid.CAR, 38));
    vehicleIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
    vehicleName.setFont(vehicleName.getFont().deriveFont(Font.BOLD, 16f));

    root.add(login, "LOGIN");

    JPanel shell = new JPanel(new BorderLayout(0, 0));
    JPanel sidebar = Ui.column();
    sidebar.setOpaque(true);
    sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));
    sidebar.setPreferredSize(new Dimension(220, 700));

    sidebar.add(vehicleIcon);
    sidebar.add(Box.createVerticalStrut(12));
    sidebar.add(vehicleName);
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(vehicleDetails);
    sidebar.add(Box.createVerticalStrut(24));

    addNavigation(
        sidebar,
        "Dashboard",
        dashboard,
            FontAwesomeSolid.TACHOMETER_ALT);
    addNavigation(
        sidebar,
        "Vozila",
        vehicles,
        FontAwesomeSolid.CAR);
    addNavigation(
        sidebar,
        "Odrzavanje",
        maintenance,
        FontAwesomeSolid.WRENCH);
    addNavigation(
        sidebar,
        "Servisi",
        services,
        FontAwesomeSolid.CLIPBOARD);
    addNavigation(
        sidebar,
        "Problemi",
        problems,
            FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    addNavigation(
        sidebar,
        "Profil",
        profile,
        FontAwesomeSolid.USER);

    sidebar.add(Box.createVerticalGlue());
    sidebar.add(Ui.hint("AutoCare / NOOP"));

    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    shell.add(sidebar, BorderLayout.WEST);
    shell.add(content, BorderLayout.CENTER);
    shell.add(Ui.row(refresh, status), BorderLayout.SOUTH);

    root.add(shell, "APP");
    setContentPane(root);
  }

  private void addNavigation(
      JPanel sidebar,
      String name,
      JPanel panel,
      FontAwesomeSolid iconCode) {
    JButton navigationButton = Ui.button(name, false);
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

      JButton navigationButton = entry.getValue();
      navigationButton.setFont(navigationButton.getFont().deriveFont(style));
    }
  }

  public String page() {
    return page;
  }

  public void context(VehicleRow vehicle) {
    vehicleName.setText(
        vehicle.getVariant().getMake() + " " + vehicle.getVariant().getModel());
    vehicleDetails.setText(
        vehicle.getYear() + " / " + Ui.km(vehicle.getMileage()));
    vehicleName.setToolTipText(
        vehicle.getVariant().getGeneration()
            + " / "
            + vehicle.getVariant().getEngine());
  }
}
