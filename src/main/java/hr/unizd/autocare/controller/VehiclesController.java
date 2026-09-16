package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.Dialog;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Dodavanje, uredjivanje, aktiviranje i brisanje vozila. */
public final class VehiclesController {

    private final MainFrame frame;
    private final VehicleService service;
    private final CatalogService catalog;
    private final Session session;
    private final AppEvents events;
    private final UiTasks tasks = new UiTasks();

    public VehiclesController(
            MainFrame frame,
            VehicleService service,
            CatalogService catalog,
            Session session,
            AppEvents events) {

        this.frame = frame;
        this.service = service;
        this.catalog = catalog;
        this.session = session;
        this.events = events;

        activateForm();
    }

    private void activateForm() {
        frame.vehicles.add.addActionListener(
                event -> edit(null));

        frame.vehicles.edit.addActionListener(
                event -> {
                    VehicleRow vehicle = selected();

                    if (vehicle != null) {
                        edit(vehicle);
                    }
                });

        frame.vehicles.activate.addActionListener(
                event -> activate());

        frame.vehicles.delete.addActionListener(
                event -> delete());
    }

    public void load() {
        tasks.read(
                frame.vehicles,
                () -> service.list(session.owner()),
                rows -> frame.vehicles.table.setRows(rows));
    }

    private VehicleRow selected() {
        VehicleRow vehicle =
                frame.vehicles.table.selected();

        if (vehicle == null) {
            Ui.info(frame, "Odaberite vozilo.");
        }

        return vehicle;
    }

    private void activate() {
        VehicleRow vehicle = selected();

        if (vehicle == null) {
            return;
        }

        tasks.write(
                frame,
                () -> {
                    service.activate(
                            session.owner(),
                            vehicle.getId());

                    return true;
                },
                result -> events.publish(
                        AppEvent.ACTIVE_VEHICLE_CHANGED));
    }

    private void edit(VehicleRow vehicle) {
        if (vehicle == null) {
            showEditor(null, true);
            return;
        }

        tasks.read(
                frame,
                () -> service.identityEditable(
                        session.owner(),
                        vehicle.getId()),
                editable -> showEditor(
                        vehicle,
                        editable));
    }

    private void showEditor(
            VehicleRow vehicle,
            boolean identityEditable) {

        JDialog dialog =
                new JDialog(
                        frame,
                        vehicle == null
                                ? "Dodaj vozilo"
                                : "Uredi vozilo",
                        Dialog.ModalityType.APPLICATION_MODAL);

        VehicleForm form = new VehicleForm();

        VehicleFormController picker =
                new VehicleFormController(
                        form,
                        catalog);

        JButton save =
                Ui.button("Spremi vozilo", true);

        JButton cancel =
                Ui.button("Odustani", false);

        JPanel root =
                new JPanel(
                        new BorderLayout(
                                12,
                                12));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20));

        root.add(form, BorderLayout.CENTER);
        root.add(
                Ui.actions(cancel, save),
                BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setSize(900, 660);
        dialog.setLocationRelativeTo(frame);

        UiTasks editorTasks = new UiTasks();

        save.addActionListener(event -> {
            try {
                VehicleInput input = form.input();

                editorTasks.write(
                        dialog,
                        () -> {
                            if (vehicle == null) {
                                service.add(
                                        session.owner(),
                                        input);
                            } else {
                                service.update(
                                        session.owner(),
                                        vehicle.getId(),
                                        input);
                            }

                            return true;
                        },
                        result -> {
                            dialog.dispose();
                            events.publish(
                                    AppEvent.VEHICLE_CHANGED);
                        });
            } catch (RuntimeException exception) {
                Ui.error(dialog, exception);
            }
        });

        Runnable close = () -> {
            if (Ui.confirm(
                    dialog,
                    "Odbaciti nespremljene promjene vozila?")) {

                dialog.dispose();
            }
        };

        cancel.addActionListener(
                event -> close.run());

        Ui.escape(dialog, close);

        if (vehicle == null) {
            SwingUtilities.invokeLater(
                    picker::loadMakes);
        } else {
            form.existing(
                    vehicle,
                    identityEditable);
        }

        dialog.setVisible(true);
    }

    private void delete() {
        VehicleRow vehicle = selected();

        if (vehicle == null) {
            return;
        }

        if (frame.vehicles.table.rows().size() <= 1) {
            Ui.info(
                    frame,
                    "Posljednje vozilo nije moguce obrisati.");
            return;
        }

        boolean confirmed =
                Ui.confirm(
                        frame,
                        "Trajno obrisati vozilo #"
                                + vehicle.getId()
                                + ", njegove servise i probleme?");

        if (!confirmed) {
            return;
        }

        tasks.write(
                frame,
                () -> {
                    service.delete(
                            session.owner(),
                            vehicle.getId());

                    return true;
                },
                result -> events.publish(
                        AppEvent.VEHICLE_CHANGED));
    }
}
