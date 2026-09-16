package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.Analysis;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.view.AnalysisDialog;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Kontrolira analizu i cuva nepromjenjivi preview odvojen od JPA entiteta. */
public final class ProblemsController {
  private final MainFrame frame;
  private final ProblemService service;
  private final Session session;
  private final AppEvents events;
  private final UiTasks tasks;

  public ProblemsController(
      MainFrame frame, ProblemService service, Session session, AppEvents events) {
    this.frame = frame;
    this.service = service;
    this.session = session;
    this.events = events;
    tasks = new UiTasks();
    frame.problems.status.addActionListener(
        e -> {
          if (session.active() != null) {
            load();
          }
        });
    frame.problems.add.addActionListener(e -> new AnalysisFlow().open());
    frame.problems.detail.addActionListener(
        e -> {
          ProblemRow p = frame.problems.table.selected();
          if (p == null) {
            Ui.info(frame, "Odaberite problem.");
            return;
          }
          Ui.info(
              frame,
              p.getDescription()
                  + "\nMoguci uzrok: "
                  + Objects.toString(p.getSuggestion(), "Nema podudaranja")
                  + "\nPodudaranje: "
                  + Objects.toString(p.getScore(), "-")
                  + " %\nProcjena: "
                  + hr.unizd.autocare.view.components.EstimateFormat.display(p.getPrice())
                  + "\nIzvor: "
                  + Objects.toString(p.getPriceNote(), "Nepoznat")
                  + "\nRijeseno servisom: "
                  + Objects.toString(p.getResolvedServiceId(), "-"));
        });
  }

  public void load() {
    long owner = session.owner(), vehicle = session.active().getId();
    ProblemStatus status =
        frame.problems.status.getSelectedIndex() == 0 ? ProblemStatus.OPEN : ProblemStatus.RESOLVED;
    tasks.read(
        frame.problems,
        () -> service.list(owner, vehicle, status),
        rows -> frame.problems.table.setRows(rows));
  }

  private final class AnalysisFlow {
    private final AnalysisDialog dialog = new AnalysisDialog(frame);
    private final UiTasks work = new UiTasks();
    private final long owner = session.owner(), vehicle = session.active().getId();
    private final String key = UUID.randomUUID().toString();
    private Analysis preview;

    void open() {
      dialog
          .description
          .getDocument()
          .addDocumentListener(
              new DocumentListener() {
                private void changed() {
                  preview = null;
                  dialog.save.setEnabled(false);
                  dialog.results.setRows(List.of());
                  dialog.estimate.setText("Opis je promijenjen; ponovno analizirajte.");
                }

                public void insertUpdate(DocumentEvent e) {
                  changed();
                }

                public void removeUpdate(DocumentEvent e) {
                  changed();
                }

                public void changedUpdate(DocumentEvent e) {
                  changed();
                }
              });
      dialog.analyze.addActionListener(
          e -> {
            String description = dialog.description.getText();
            work.read(
                dialog,
                () -> service.analyze(owner, vehicle, description),
                a -> {
                  if (!description.equals(dialog.description.getText())) {
                    return;
                  }
                  preview = a;
                  dialog.results.setRows(a.getResults());
                  dialog.save.setEnabled(true);
                  dialog.estimate.setText(
                      a.getResults().isEmpty()
                          ? "Nema podudaranja. Mozete spremiti opis bez pretpostavljenog uzroka."
                          : "Glavna informativna procjena: "
                              + hr.unizd.autocare.view.components.EstimateFormat.display(
                                  a.getResults().get(0).getPrice()));
                });
          });
      dialog.save.addActionListener(
          e -> {
            Analysis snapshot = preview;
            if (snapshot == null) {
              return;
            }
            work.run(
                dialog,
                () -> service.save(owner, vehicle, key, snapshot),
                id -> {
                  dialog.dispose();
                  events.publish(AppEvent.PROBLEM_SAVED);
                },
                error -> Ui.error(dialog, error));
          });
      Runnable close =
          () -> {
            if (dialog.description.getText().isBlank()
                || Ui.confirm(dialog, "Zatvoriti i odbaciti nespremljenu analizu?")) {
              dialog.dispose();
            }
          };
      dialog.cancel.addActionListener(e -> close.run());
      Ui.escape(dialog, close);
      dialog.setVisible(true);
    }
  }
}
