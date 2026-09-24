package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.model.Data.ServiceDetail;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Servisna povijest, detalj servisa i unos novog servisa. */
public final class ServicesController {
  private final MainFrame frame;
  private final ServiceRecordService serviceRecordService;
  private final CatalogService catalogService;
  private final ProblemService problemService;
  private final Session session;
  private final Subject subject;

  public ServicesController(
      MainFrame frame,
      ServiceRecordService serviceRecordService,
      CatalogService catalogService,
      ProblemService problemService,
      Session session,
      Subject subject) {
    this.frame = frame;
    this.serviceRecordService = serviceRecordService;
    this.catalogService = catalogService;
    this.problemService = problemService;
    this.session = session;
    this.subject = subject;
    frame.services.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            create();
          }
        });
    frame.services.detail.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            detail();
          }
        });
  }

  public void load() {
    try {
      frame.services.setRows(
          serviceRecordService.list(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.services, exception);
    }
  }

  private void detail() {
    ServiceRow selectedService = frame.services.selected();
    if (selectedService == null) {
      Ui.info(frame, "Odaberite servis.");
      return;
    }
    try {
      ServiceDetail detail =
          serviceRecordService.detail(session.getOwnerId(), selectedService.getId());
      frame.services.showServiceDetails(detail);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void create() {
    int ownerId = session.getOwnerId();
    int vehicleId = session.getActiveVehicle().getId();
    int currentMileage = session.getActiveVehicle().getMileage();
    try {
      List<WorkDefinition> works = loadEditorWorks();
      List<Problem> openProblems = new ArrayList<>();
      for (Problem problem : problemService.list(ownerId, vehicleId)) {
        if (!problem.isResolved()) {
          openProblems.add(problem);
        }
      }
      openEditor(ownerId, vehicleId, currentMileage, works, openProblems);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private List<WorkDefinition> loadEditorWorks() {
    List<WorkDefinition> works =
        new ArrayList<>(catalogService.works(WorkCategory.MAINTENANCE));
    works.addAll(catalogService.works(WorkCategory.REPAIR));
    return works;
  }

  private void openEditor(
      final int ownerId,
      final int vehicleId,
      int currentMileage,
      List<WorkDefinition> works,
      List<Problem> openProblems) {
    final ServiceEditorDialog dialog =
        new ServiceEditorDialog(frame, currentMileage, openProblems);
    new ServiceEditorController(
        dialog,
        works,
        new ServiceEditorListener() {
          @Override
          public void saveService(ServiceInput serviceInput) {
            serviceRecordService.create(ownerId, vehicleId, serviceInput);
            dialog.dispose();
            subject.notifyObservers(AppEvent.SERVICE_SAVED);
          }
        });
    dialog.setVisible(true);
  }
}
