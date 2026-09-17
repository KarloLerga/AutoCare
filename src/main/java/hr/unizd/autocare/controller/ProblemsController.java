package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.ProblemEstimate;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/** Ručni opis problema, odabir mogućeg popravka i procjena iz pravila vozila. */
public final class ProblemsController {
  private final MainFrame frame;
  private final ProblemService problemService;
  private final CatalogService catalogService;
  private final Session session;
  private final AppEvents events;

  public ProblemsController(
      MainFrame frame,
      ProblemService problemService,
      CatalogService catalogService,
      Session session,
      AppEvents events) {
    this.frame = frame;
    this.problemService = problemService;
    this.catalogService = catalogService;
    this.session = session;
    this.events = events;

    frame.problems.estimate.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            estimate();
          }
        });
    frame.problems.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            save();
          }
        });
  }

  public void load() {
    long ownerId = session.getOwnerId();
    long vehicleId = session.getActiveVehicle().getId();
    try {
      frame.problems.setRows(problemService.list(ownerId, vehicleId));
      frame.problems.setRepairs(
          catalogService.works(ownerId, vehicleId, WorkCategory.REPAIR));
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void estimate() {
    WorkRow selected = frame.problems.selectedRepair();
    if (selected == null) {
      Ui.info(frame.problems, "Odaberite popravak.");
      return;
    }
    try {
      ProblemEstimate estimate =
          problemService.estimate(
              session.getOwnerId(), session.getActiveVehicle().getId(), selected.getId());
      frame.problems.showEstimate(estimate);
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  private void save() {
    WorkRow selected = frame.problems.selectedRepair();
    if (selected == null) {
      Ui.info(frame.problems, "Odaberite popravak prije spremanja.");
      return;
    }
    try {
      problemService.create(
          session.getOwnerId(),
          session.getActiveVehicle().getId(),
          frame.problems.description.getText(),
          selected.getId());
      frame.problems.clearEditor();
      events.publish(AppEvent.PROBLEM_SAVED);
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

}
