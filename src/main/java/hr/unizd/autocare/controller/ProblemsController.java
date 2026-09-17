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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Povezuje ekran problema s poslovnim use-caseovima. */
public final class ProblemsController {
  private final MainFrame frame;
  private final ProblemService problemService;
  private final Session session;
  private final AppEvents events;

  private AnalysisDialog analysisDialog;
  private Analysis analysisPreview;
  private long analysisOwnerId;
  private long analysisVehicleId;

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
            showSelectedProblem();
          }
        });
  }

  public void load() {
    long ownerId = session.owner();
    long vehicleId = session.active().getId();

    ProblemStatus problemStatus = ProblemStatus.OPEN;
    if (frame.problems.status.getSelectedIndex() == 1) {
      problemStatus = ProblemStatus.RESOLVED;
    }

    try {
      frame.problems.setRows(problemService.list(ownerId, vehicleId, problemStatus));
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void showSelectedProblem() {
    ProblemRow problem = frame.problems.selected();
    if (problem == null) {
      Ui.info(frame, "Odaberite problem.");
      return;
    }

    frame.problems.showProblemDetails(problem);
  }

  private void openAnalysis() {
    analysisOwnerId = session.owner();
    analysisVehicleId = session.active().getId();
    analysisPreview = null;
    analysisDialog = new AnalysisDialog(frame);

    analysisDialog.analyze.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            analyze();
          }
        });

    analysisDialog.save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            saveAnalysis();
          }
        });

    analysisDialog.cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            analysisDialog.dispose();
          }
        });

    Ui.escape(analysisDialog);
    analysisDialog.setVisible(true);
  }

  private void analyze() {
    try {
      String description = analysisDialog.description.getText();
      analysisPreview =
          problemService.analyze(analysisOwnerId, analysisVehicleId, description);

      analysisDialog.setResults(analysisPreview.getResults());
      analysisDialog.save.setEnabled(true);

      if (analysisPreview.getResults().isEmpty()) {
        analysisDialog.estimate.setText(
            "Nema podudaranja. Mozete spremiti opis bez pretpostavljenog uzroka.");
      } else {
        analysisDialog.estimate.setText(
            "Glavna informativna procjena: "
                + Ui.estimate(analysisPreview.getResults().get(0).getPrice()));
      }
    } catch (RuntimeException exception) {
      Ui.error(analysisDialog, exception);
    }
  }

  private void saveAnalysis() {
    if (analysisPreview == null) {
      Ui.info(analysisDialog, "Prvo analizirajte opis.");
      return;
    }

    String currentDescription = analysisDialog.description.getText().strip();
    if (!currentDescription.equals(analysisPreview.getDescription())) {
      Ui.info(analysisDialog, "Opis je promijenjen. Ponovno pokrenite analizu.");
      return;
    }

    try {
      problemService.save(analysisOwnerId, analysisVehicleId, analysisPreview);
      analysisDialog.dispose();
      events.publish(AppEvent.PROBLEM_SAVED);
    } catch (RuntimeException exception) {
      Ui.error(analysisDialog, exception);
    }
  }
}
