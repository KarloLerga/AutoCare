package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.event.AppListener;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.DashboardService;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/** Glavna navigacija, session i osvjezavanje trenutno otvorenog ekrana. */
public final class MainController implements AppListener {

    private final MainFrame frame;
    private final Session session;
    private final VehicleService vehicleService;
    private final DashboardService dashboardService;
    private final AppEvents events;
    private final UiTasks tasks;
    private final Runnable shutdown;

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
            Runnable shutdown) {

        this.frame = frame;
        this.session = session;
        this.vehicleService = vehicleService;
        this.dashboardService = dashboardService;
        this.events = events;
        this.shutdown = shutdown;
        this.tasks = new UiTasks();

        vehicles = new VehiclesController(
                frame,
                vehicleService,
                catalogService,
                session,
                events);

        services = new ServicesController(
                frame,
                serviceRecordService,
                catalogService,
                problemService,
                session,
                events);

        maintenance = new MaintenanceController(
                frame,
                maintenanceService,
                session);

        problems = new ProblemsController(
                frame,
                problemService,
                session,
                events);

        profile = new ProfileController(
                frame,
                authService,
                session,
                events,
                this::logout);

        new AuthController(
                frame,
                authService,
                catalogService,
                session,
                this::enter);

        activateForm();
        events.add(this);
        frame.auth();
    }

    private void activateForm() {
        frame.navigation.forEach(
                (name, button) ->
                        button.addActionListener(
                                event -> navigate(name)));

        frame.refresh.addActionListener(
                event -> refreshContext(false));

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

    private void refreshContext(boolean dashboard) {
        if (session.owner() == 0) {
            return;
        }

        tasks.read(
                frame,
                () -> vehicleService.active(session.owner()),
                vehicle -> {
                    session.setActive(vehicle);
                    frame.context(vehicle);
                    frame.application();

                    if (dashboard) {
                        frame.showPage("Dashboard");
                    }

                    loadVisible();
                });
    }

    private void navigate(String name) {
        if (frame.page().equals("Profil")
                && !profile.canLeave()) {
            return;
        }

        frame.showPage(name);
        loadVisible();
    }

    private void loadVisible() {
        if (session.active() == null) {
            return;
        }

        frame.status.setText(
                "Aktivno vozilo #"
                        + session.active().getId());

        switch (frame.page()) {
            case "Dashboard" -> loadDashboard();
            case "Vozila" -> vehicles.load();
            case "Servisi" -> services.load();
            case "Odrzavanje" -> maintenance.load();
            case "Problemi" -> problems.load();
            case "Profil" -> profile.load();
            default -> throw new IllegalArgumentException(
                    "Nepoznat ekran.");
        }
    }

    private void loadDashboard() {
        long ownerId = session.owner();
        long vehicleId = session.active().getId();

        tasks.read(
                frame.dashboard,
                () -> dashboardService.get(
                        ownerId,
                        vehicleId),
                data -> frame.dashboard.show(data));
    }

    @Override
    public void onChange(AppEvent event) {
        if (event == AppEvent.ACTIVE_VEHICLE_CHANGED
                || event == AppEvent.VEHICLE_CHANGED
                || event == AppEvent.SERVICE_SAVED) {

            refreshContext(
                    event == AppEvent.ACTIVE_VEHICLE_CHANGED);

            return;
        }

        loadVisible();
    }

    private void logout() {
        session.logout();

        profile.clear();
        frame.login.password.setText("");

        frame.vehicles.table.setRows(List.of());
        frame.services.table.setRows(List.of());
        frame.maintenance.table.setRows(List.of());
        frame.problems.table.setRows(List.of());

        frame.auth();
    }

    private void close() {
        if (session.owner() != 0
                && frame.page().equals("Profil")
                && !profile.canLeave()) {
            return;
        }

        events.remove(this);
        session.logout();
        frame.dispose();
        shutdown.run();
    }
}


