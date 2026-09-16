package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.ItemRow;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Servisna povijest, detalj servisa i unos novog servisa. */
public final class ServicesController {

    private final MainFrame frame;
    private final ServiceRecordService service;
    private final CatalogService catalog;
    private final ProblemService problems;
    private final Session session;
    private final AppEvents events;
    private final UiTasks tasks;

    public ServicesController(
            MainFrame frame,
            ServiceRecordService service,
            CatalogService catalog,
            ProblemService problems,
            Session session,
            AppEvents events) {

        this.frame = frame;
        this.service = service;
        this.catalog = catalog;
        this.problems = problems;
        this.session = session;
        this.events = events;
        tasks = new UiTasks();

        frame.services.add.addActionListener(event -> create());
        frame.services.detail.addActionListener(event -> detail());
    }

    public void load() {
        long ownerId = session.owner();
        long vehicleId = session.active().getId();

        tasks.read(
                frame.services,
                () -> service.list(ownerId, vehicleId),
                rows -> frame.services.table.setRows(rows));
    }

    private void detail() {
        ServiceRow selected = frame.services.table.selected();

        if (selected == null) {
            Ui.info(frame, "Odaberite servis.");
            return;
        }

        tasks.read(
                frame,
                () -> service.detail(session.owner(), selected.getId()),
                detail -> {
                    StringBuilder text = new StringBuilder();

                    text.append(Ui.date(detail.getHeader().getDate()));
                    text.append(" / ");
                    text.append(Ui.km(detail.getHeader().getMileage()));
                    text.append("\n\n");

                    for (ItemRow item : detail.getItems()) {
                        text.append(item.getName());
                        text.append(": ");
                        text.append(Ui.money(item.getActualPrice()));
                        text.append("\n");
                    }

                    text.append("\nUkupno: ");
                    text.append(Ui.total(detail.getHeader().getTotal()));
                    text.append("\nNapomena: ");
                    text.append(Objects.toString(
                            detail.getHeader().getNote(),
                            "-"));

                    text.append("\n\nRijeseni problemi:\n");

                    for (String problem : detail.getResolvedProblems()) {
                        text.append(problem);
                        text.append("\n");
                    }

                    JTextArea area = new JTextArea(text.toString(), 18, 65);
                    area.setEditable(false);
                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);

                    JOptionPane.showMessageDialog(
                            frame,
                            new JScrollPane(area),
                            "Detalj servisa",
                            JOptionPane.INFORMATION_MESSAGE);
                });
    }

    private void create() {
        long ownerId = session.owner();
        long vehicleId = session.active().getId();
        int mileage = session.active().getMileage();

        tasks.read(
                frame,
                () -> loadEditorData(ownerId, vehicleId),
                data -> openEditor(ownerId, vehicleId, mileage, data));
    }

    private EditorData loadEditorData(long ownerId, long vehicleId) {
        List<WorkRow> works = new ArrayList<>(
                catalog.works(
                        ownerId,
                        vehicleId,
                        WorkCategory.MAINTENANCE));

        works.addAll(
                catalog.works(
                        ownerId,
                        vehicleId,
                        WorkCategory.REPAIR));

        List<ProblemRow> openProblems = problems.list(
                ownerId,
                vehicleId,
                ProblemStatus.OPEN);

        return new EditorData(works, openProblems);
    }

    private void openEditor(
            long ownerId,
            long vehicleId,
            int mileage,
            EditorData data) {

        ServiceEditorDialog dialog =
                new ServiceEditorDialog(
                        frame,
                        mileage,
                        false,
                        data.problems);

        UiTasks editorTasks = new UiTasks();

        new ServiceEditorController(
                dialog,
                data.works,
                input -> editorTasks.write(
                        dialog,
                        () -> service.create(
                                ownerId,
                                vehicleId,
                                input),
                        id -> {
                            dialog.dispose();
                            events.publish(AppEvent.SERVICE_SAVED);
                        }));

        dialog.setVisible(true);
    }

    private static final class EditorData {

        private final List<WorkRow> works;
        private final List<ProblemRow> problems;

        private EditorData(
                List<WorkRow> works,
                List<ProblemRow> problems) {

            this.works = works;
            this.problems = problems;
        }
    }
}
