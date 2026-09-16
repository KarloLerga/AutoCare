package hr.unizd.autocare.controller;
import hr.unizd.autocare.app.*;
import hr.unizd.autocare.event.*;
import hr.unizd.autocare.service.*;
import hr.unizd.autocare.view.*;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
/** Samo shell/navigacija. Svaki use-case ima svoj kontroler i service. */
public final class MainController implements AppListener {
    private final MainFrame frame;
    private final Session session;
    private final VehicleService vehicleService;
    private final DashboardService dashboard;
    private final AppEvents events;
    private final UiTasks tasks, dashboardTasks;
    private final Runnable shutdown;
    private final VehiclesController vehicles;
    private final ServicesController services;
    private final MaintenanceController maintenance;
    private final ProblemsController problems;
    private final ProfileController profile;
    private final Set<String> dirty=new HashSet<>();
    public MainController(MainFrame frame, Session session, AuthService auth, CatalogService catalog, VehicleService vehicleService, ServiceRecordService serviceRecords, MaintenanceService maintenanceService, ProblemService problemService, DashboardService dashboard, AppEvents events, Runnable shutdown) {
        this.frame=frame;
        this.session=session;
        this.vehicleService=vehicleService;
        this.dashboard=dashboard;
        this.events=events;
        this.shutdown=shutdown;
        tasks=new UiTasks(session);
        dashboardTasks=new UiTasks(session);
        vehicles=new VehiclesController(frame, vehicleService, catalog, session, events);
        services=new ServicesController(frame, serviceRecords, catalog, problemService, session, events);
        maintenance=new MaintenanceController(frame, maintenanceService, session);
        problems=new ProblemsController(frame, problemService, session, events);
        profile=new ProfileController(frame, auth, session, events, this::logout);
        new AuthController(frame, auth, catalog, session, this::enter);
        frame.navigation.forEach((name, button)->button.addActionListener(e->navigate(name)));
        frame.refresh.addActionListener(e-> {
            if(session.isWriting())return;
            if(frame.page().equals("Profil")&&!profile.canLeave())return;
            dirty.add(frame.page());
            refreshContext(false);
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        events.add(this);
        frame.auth();
    }
    private void enter(long owner) {
        session.login(owner);
        dirty.addAll(frame.navigation.keySet());
        refreshContext(true);
    }
    private void refreshContext(boolean showDashboard) {
        long owner=session.owner();
        tasks.read(frame, ()->vehicleService.active(owner), vehicle-> {
            session.setActive(vehicle);
            frame.context(vehicle);
            frame.application();
            if(showDashboard)frame.showPage("Dashboard");
            loadVisible();
        });
    }
    private void navigate(String name) {
        if(session.isWriting()) {
            Ui.info(frame, "Pricekajte spremanje.");
            return;
        }
        if(frame.page().equals("Profil")&&!profile.canLeave())return;
        frame.showPage(name);
        loadVisible();
    }
    private void loadVisible() {
        if(session.active()==null||!dirty.remove(frame.page()))return;
        String page=frame.page();
        frame.status.setText("Aktivno vozilo #"+session.active().getId());
        switch(page) {
            case "Dashboard"-> {
                long owner=session.owner(), vehicle=session.active().getId();
                dashboardTasks.read(frame.dashboard, ()->dashboard.get(owner, vehicle), data->frame.dashboard.show(data));
            }
            case "Vozila"->vehicles.load();
            case "Servisi"->services.load();
            case "Odrzavanje"->maintenance.load();
            case "Problemi"->problems.load();
            case "Profil"->profile.load();
            default->throw new IllegalArgumentException("Nepoznat ekran.");
        }
    }
    @Override public void onChange(AppEvent event) {
        dirty.addAll(frame.navigation.keySet());
        boolean activeChanged=event==AppEvent.ACTIVE_VEHICLE_CHANGED;
        if(event==AppEvent.VEHICLE_CHANGED||activeChanged||event==AppEvent.SERVICE_SAVED)refreshContext(activeChanged);
        else loadVisible();
    }
    private void logout() {
        if(session.isWriting())return;
        session.logout();
        profile.clear();
        frame.login.password.setText("");
        frame.vehicles.table.setRows(List.of());
        frame.services.table.setRows(List.of());
        frame.maintenance.table.setRows(List.of());
        frame.problems.table.setRows(List.of());
        frame.auth();
        dirty.clear();
    }
    private void close() {
        if(session.isWriting()) {
            Ui.info(frame, "Spremanje je u tijeku; pricekajte potvrdu.");
            return;
        }
        if(session.owner()!=0&&frame.page().equals("Profil")&&!profile.canLeave())return;
        events.remove(this);
        events.clear();
        session.logout();
        frame.dispose();
        shutdown.run();
    }
}
