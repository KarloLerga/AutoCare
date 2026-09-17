package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.model.Data.ServiceDetail;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.model.Data.WorkRow;
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
  private final AppEvents events;

  public ServicesController(
      MainFrame frame,
      ServiceRecordService serviceRecordService,
      CatalogService catalogService,
      ProblemService problemService,
      Session session,
      AppEvents events) {
    this.frame = frame;
    this.serviceRecordService = serviceRecordService;
    this.catalogService = catalogService;
    this.problemService = problemService;
    this.session = session;
    this.events = events;
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
          serviceRecordService.list(session.owner(), session.active().getId()));
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
      ServiceDetail detail = serviceRecordService.detail(session.owner(), selectedService.getId());
      frame.services.showServiceDetails(detail);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void create() {
    long ownerId = session.owner();
    long vehicleId = session.active().getId();
    int currentMileage = session.active().getMileage();
    try {
      List<WorkRow> works = loadEditorWorks(ownerId, vehicleId);
      List<ProblemRow> openProblems =
          problemService.list(ownerId, vehicleId, ProblemStatus.OPEN);
      openEditor(ownerId, vehicleId, currentMileage, works, openProblems);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private List<WorkRow> loadEditorWorks(long ownerId, long vehicleId) {
    List<WorkRow> works =
        new ArrayList<>(
            catalogService.works(ownerId, vehicleId, WorkCategory.MAINTENANCE));
    works.addAll(catalogService.works(ownerId, vehicleId, WorkCategory.REPAIR));
    return works;
  }

  private void openEditor(
      final long ownerId,
      final long vehicleId,
      int currentMileage,
      List<WorkRow> works,
      List<ProblemRow> openProblems) {
    final ServiceEditorDialog dialog =
        new ServiceEditorDialog(frame, currentMileage, false, openProblems);
    new ServiceEditorController(
        dialog,
        works,
        new ServiceEditorListener() {
          @Override
          public void saveService(ServiceInput serviceInput) {
            serviceRecordService.create(ownerId, vehicleId, serviceInput);
            dialog.dispose();
            events.publish(AppEvent.SERVICE_SAVED);
          }
        });
    dialog.setVisible(true);
  }
}
