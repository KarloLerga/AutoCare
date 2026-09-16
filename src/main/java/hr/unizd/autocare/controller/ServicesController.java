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

/** Servisna povijest, detalj i jedan atomarni save drafta. */
public final class ServicesController {
  private final MainFrame frame;
  private final ServiceRecordService service;
  private final CatalogService catalog;
  private final ProblemService problems;
  private final Session session;
  private final AppEvents events;
  private final UiTasks tasks;
  private int offset;
  private long lastVehicle = -1;

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
    frame.services.add.addActionListener(e -> create());
    frame.services.detail.addActionListener(e -> detail());
    frame.services.previous.addActionListener(
        e -> {
          offset = Math.max(0, offset - 50);
          load();
        });
    frame.services.next.addActionListener(
        e -> {
          offset += 50;
          load();
        });
  }

  public void load() {
    long owner = session.owner(), vehicle = session.active().getId();
    if (lastVehicle != vehicle) {
      offset = 0;
      lastVehicle = vehicle;
    }
    int start = offset;
    tasks.read(
        frame.services,
        () -> service.page(owner, vehicle, start),
        rows -> {
          frame.services.table.setRows(rows);
          frame.services.previous.setEnabled(start > 0);
          frame.services.next.setEnabled(rows.size() == 50);
          frame.services.page.setText(
              "Stranica " + (start / 50 + 1) + (rows.isEmpty() ? " - nema zapisa" : ""));
        });
  }

  private void detail() {
    ServiceRow row = frame.services.table.selected();
    if (row == null) {
      Ui.info(frame, "Odaberite servis.");
      return;
    }
    long owner = session.owner();
    tasks.read(
        frame,
        () -> service.detail(owner, row.getId()),
        detail -> {
          StringBuilder text =
              new StringBuilder(
                  Ui.date(detail.getHeader().getDate())
                      + " / "
                      + Ui.km(detail.getHeader().getMileage())
                      + "\n\n");
          for (ItemRow i : detail.getItems()) {
            text.append(i.getName()).append(": ").append(Ui.money(i.getActualPrice())).append("\n");
          }
          text.append("\nPoznati zbroj: ")
              .append(Ui.total(detail.getHeader().getTotal()))
              .append("\nNapomena: ")
              .append(Objects.toString(detail.getHeader().getNote(), "-"))
              .append("\n\nRijeseni problemi:\n");
          for (String p : detail.getResolvedProblems()) {
            text.append(p).append("\n");
          }
          JTextArea area = new JTextArea(text.toString(), 18, 65);
          area.setEditable(false);
          area.setLineWrap(true);
          area.setWrapStyleWord(true);
          JOptionPane.showMessageDialog(
              frame, new JScrollPane(area), "Detalj servisa", JOptionPane.INFORMATION_MESSAGE);
        });
  }

  private void create() {
    long owner = session.owner(), vehicle = session.active().getId();
    int km = session.active().getMileage();
    tasks.read(
        frame,
        () -> {
          List<WorkRow> works =
              new ArrayList<>(catalog.works(owner, vehicle, WorkCategory.MAINTENANCE));
          works.addAll(catalog.works(owner, vehicle, WorkCategory.REPAIR));
          return new EditorData(works, problems.list(owner, vehicle, ProblemStatus.OPEN));
        },
        data -> {
          ServiceEditorDialog dialog = new ServiceEditorDialog(frame, km, false, data.problems);
          UiTasks editorTask = new UiTasks();
          new ServiceEditorController(
              dialog,
              data.works,
              input ->
                  editorTask.run(
                      dialog,
                      () -> service.create(owner, vehicle, input),
                      id -> {
                        dialog.dispose();
                        events.publish(AppEvent.SERVICE_SAVED);
                      },
                      error -> Ui.error(dialog, error)));
          dialog.setVisible(true);
        });
  }

  private static final class EditorData {
    final List<WorkRow> works;
    final List<ProblemRow> problems;

    EditorData(List<WorkRow> works, List<ProblemRow> problems) {
      this.works = works;
      this.problems = problems;
    }
  }
}
