package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Spremanje, prikaz i zatvaranje korisnikovih problema za aktivno vozilo. */
public final class ProblemsController {
  private final MainFrame frame;
  private final ProblemService problemService;
  private final Session session;

  public ProblemsController(
      MainFrame frame, ProblemService problemService, Session session) {
    this.frame = frame;
    this.problemService = problemService;
    this.session = session;

    frame.problems.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            save();
          }
        });
    frame.problems.close.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            closeSelected();
          }
        });
  }

  public void load() {
    try {
      frame.problems.setRows(
          problemService.list(
              session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void save() {
    try {
      problemService.create(
          session.getOwnerId(),
          session.getActiveVehicle().getId(),
          frame.problems.description.getText(),
          frame.problems.selectedCategory());
      frame.problems.clearEditor();
      load();
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void closeSelected() {
    ProblemRow selected = frame.problems.selectedRow();
    if (selected == null) {
      Ui.info(frame.problems, "Odaberite problem.");
      return;
    }
    if (!frame.problems.selectedIsOpen()) {
      Ui.info(frame.problems, "Odabrani problem je već zatvoren.");
      return;
    }
    try {
      problemService.close(session.getOwnerId(), selected.getId());
      load();
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }
}
