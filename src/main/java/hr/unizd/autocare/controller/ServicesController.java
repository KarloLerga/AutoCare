package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.ServiceDetail;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Subject;
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

/**
 * Upravlja servisnom poviješću, detaljem servisa i unosom novog servisa aktivnog vozila.
 *
 * <p>Controller povezuje akcije Viewa s servisima za zapise, katalog i probleme. Nakon uspješnog
 * spremanja šalje Observer događaj kako bi ovisni prikazi mogli ponovno učitati podatke.
 */
public class ServicesController {
  /** Glavni prozor kroz koji Controller dohvaća ServicesView i otvara modalni editor. */
  private final MainFrame frame;

  /** Service use-caseovi za dohvat servisne povijesti, detalja i spremanje servisa. */
  private final ServiceRecordService serviceRecordService;

  /** Dohvaća katalog radova koji se mogu dodati u novi servis. */
  private final CatalogService catalogService;

  /** Dohvaća otvorene probleme koje korisnik može povezati s novim servisom. */
  private final ProblemService problemService;

  /** Daje Controlleru ID prijavljenog korisnika i trenutačno aktivno vozilo. */
  private final Session session;

  /** Objavljuje događaj nakon spremanja kako bi se osvježili ovisni ekrani. */
  private final Subject subject;

  /**
   * Povezuje ServicesView sa servisima potrebnim za servisnu povijest i unos novog servisa.
   *
   * @param frame glavni prozor
   * @param serviceRecordService servis za spremanje i čitanje servisa
   * @param catalogService servis za radove koji se mogu dodati u servis
   * @param problemService servis za otvorene probleme vozila
   * @param session zajednički korisnički kontekst
   * @param subject Subject za objavu događaja nakon spremanja servisa
   */
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

    frame.services.add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        create();
      }
    });

    frame.services.detail.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        detail();
      }
    });
  }

  /** Učitava servisnu povijest trenutačno aktivnog vozila i predaje retke ServicesViewu. */
  public void load() {
    try {
      frame.services.setRows(serviceRecordService.list(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.services, exception);
    }
  }

  /** Dohvaća detalj odabranog servisa i prikazuje njegove stavke, ukupni trošak i riješene probleme. */
  private void detail() {
    ServiceRow selectedService = frame.services.selected();
    if (selectedService == null) {
      Ui.info(frame, "Odaberite servis.");
      return;
    }

    try {
      ServiceDetail detail = serviceRecordService.detail(session.getOwnerId(), selectedService.getId());
      frame.services.showServiceDetails(detail);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  /**
   * Pokreće unos novog servisa za aktivno vozilo.
   *
   * <p>Učitava katalog radova i otvorene probleme, otvara ServiceEditorDialog te nakon potvrde
   * sprema jedan ServiceInput kroz ServiceRecordService. Nakon uspjeha osvježava servisnu povijest
   * i objavljuje SERVICE_SAVED događaj.
   */
  private void create() {
    int ownerId = session.getOwnerId();
    int vehicleId = session.getActiveVehicle().getId();
    int currentMileage = session.getActiveVehicle().getCurrentMileage();

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

  /**
   * Dohvaća skup radova koji se mogu odabrati u editoru servisa.
   *
   * @return dostupne definicije radova
   */
  private List<WorkDefinition> loadEditorWorks() {
    List<WorkDefinition> works = new ArrayList<>(catalogService.works(WorkCategory.MAINTENANCE));
    works.addAll(catalogService.works(WorkCategory.REPAIR));
    return works;
  }

  /**
   * Stvara i konfigurira modalni editor novog servisa.
   *
   * <p>Metoda povezuje promjenu vrste rada, dodavanje i uklanjanje stavki, odustajanje i završno
   * spremanje sa ServiceEditorDialogom.
   *
   * @param ownerId identifikator prijavljenog korisnika
   * @param vehicleId identifikator aktivnog vozila
   * @param currentMileage trenutačna kilometraža vozila koja se nudi kao početna vrijednost
   * @param works radovi dostupni za unos
   * @param openProblems otvoreni problemi koje je moguće označiti kao riješene
   */
  private void openEditor(
      final int ownerId,
      final int vehicleId,
      int currentMileage,
      List<WorkDefinition> works,
      List<Problem> openProblems) {
    final ServiceEditorDialog dialog = new ServiceEditorDialog(frame, currentMileage, openProblems);
    dialog.setWorks(new ArrayList<>(works));

    dialog.type.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        dialog.filterWorks();
      }
    });

    dialog.addItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          WorkDefinition selected = dialog.selectedWork();
          if (selected == null) {
            throw new IllegalArgumentException("Odaberite rad.");
          }
          dialog.addWork(selected);
        } catch (RuntimeException exception) {
          Ui.error(dialog, exception);
        }
      }
    });

    dialog.remove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        dialog.removeSelectedItem();
      }
    });

    dialog.save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          serviceRecordService.create(ownerId, vehicleId, dialog.input());
          dialog.dispose();
          subject.notifyObservers(AppEvent.SERVICE_SAVED);
        } catch (RuntimeException exception) {
          Ui.error(dialog, exception);
        }
      }
    });

    dialog.cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        dialog.dispose();
      }
    });

    dialog.setVisible(true);
  }
}
