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
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Kontrolira analizu i cuva rezultat koji je korisnik vidio. */
public final class ProblemsController {
  private final MainFrame frame;
  private final ProblemService problemService;
  private final Session session;
  private final AppEvents events;

  public ProblemsController(
      MainFrame frame, ProblemService problemService, Session session, AppEvents events) {
    this.frame = frame;
    this.problemService = problemService;
    this.session = session;
    this.events = events;
    frame.problems.status.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            if (session.active() != null) {
              load();
            }
          }
        });
    frame.problems.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            openAnalysis();
          }
        });
    frame.problems.detail.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            ProblemRow problem = frame.problems.selected();
            if (problem == null) {
              Ui.info(frame, "Odaberite problem.");
            } else {
              frame.problems.showProblemDetails(problem);
            }
          }
        });
  }

  public void load() {
    long ownerId = session.owner();
    long vehicleId = session.active().getId();
    ProblemStatus status = ProblemStatus.OPEN;
    if (frame.problems.status.getSelectedIndex() == 1) {
      status = ProblemStatus.RESOLVED;
    }
    try {
      frame.problems.setRows(problemService.list(ownerId, vehicleId, status));
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void openAnalysis() {
    new AnalysisFlow().open();
  }

  private final class AnalysisFlow {
    private final AnalysisDialog dialog = new AnalysisDialog(frame);
    private final long ownerId = session.owner();
    private final long vehicleId = session.active().getId();
    private Analysis preview;

    private void open() {
      dialog.description.getDocument().addDocumentListener(
          new DocumentListener() {
            private void descriptionChanged() {
              preview = null;
              dialog.save.setEnabled(false);
              dialog.setResults(new ArrayList<>());
              dialog.estimate.setText("Opis je promijenjen; ponovno analizirajte.");
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
              descriptionChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
              descriptionChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
              descriptionChanged();
            }
          });
      dialog.analyze.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
              analyze();
            }
          });
      dialog.save.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
              save();
            }
          });
      dialog.cancel.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
              dialog.dispose();
            }
          });
      Ui.escape(dialog);
      dialog.setVisible(true);
    }

    private void analyze() {
      try {
        String description = dialog.description.getText();
        preview = problemService.analyze(ownerId, vehicleId, description);
        dialog.setResults(preview.getResults());
        dialog.save.setEnabled(true);
        if (preview.getResults().isEmpty()) {
          dialog.estimate.setText(
              "Nema podudaranja. Mozete spremiti opis bez pretpostavljenog uzroka.");
        } else {
          dialog.estimate.setText(
              "Glavna informativna procjena: "
                  + EstimateFormat.display(preview.getResults().get(0).getPrice()));
        }
      } catch (RuntimeException exception) {
        Ui.error(dialog, exception);
      }
    }

    private void save() {
      if (preview == null) {
        return;
      }
      try {
        problemService.save(ownerId, vehicleId, preview);
        dialog.dispose();
        events.publish(AppEvent.PROBLEM_SAVED);
      } catch (RuntimeException exception) {
        Ui.error(dialog, exception);
      }
    }
  }
}
