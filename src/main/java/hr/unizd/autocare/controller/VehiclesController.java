package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.MileageDialog;
import hr.unizd.autocare.view.VehicleDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Upravlja pregledom, dodavanjem, promjenom kilometraže i aktiviranjem korisnikovih vozila.
 */
public class VehiclesController {
  /** Glavni prozor koji sadrži popis vozila i dijaloge za unos njihovih podataka. */
  private final MainFrame frame;

  /** Obavlja use-caseove dohvata, spremanja i izmjene korisnikovih vozila. */
  private final VehicleService vehicleService;

  /** Dohvaća kataloške vrijednosti potrebne pri unosu vozila. */
  private final CatalogService catalogService;

  /** Daje identitet prijavljenog korisnika i aktivni kontekst sučelja. */
  private final Session session;

  /** Obavještava druge dijelove aplikacije nakon promjene ili odabira vozila. */
  private final Subject subject;

  /**
   * Stvara Controller vozila i povezuje ga s Viewom, servisima, sesijom i Observer Subjectom.
   *
   * @param frame glavni prozor koji sadrži VehiclesView
   * @param vehicleService servis za rad s korisnikovim vozilima
   * @param catalogService servis kataloga potreban pri dodavanju vozila
   * @param session zajednički korisnički kontekst
   * @param subject Subject preko kojeg se objavljuju promjene vozila
   */
  public VehiclesController(
      MainFrame frame,
      VehicleService vehicleService,
      CatalogService catalogService,
      Session session,
      Subject subject) {
    this.frame = frame;
    this.vehicleService = vehicleService;
    this.catalogService = catalogService;
    this.session = session;
    this.subject = subject;
    registerListeners();
  }

  /**
   * Veže tri akcije u VehiclesView uz njihove Controller tokove.
   *
   * <p>Gumbi za dodavanje, promjenu kilometraže i aktiviranje delegiraju na imenovane metode, koje
   * zatim obavljaju validaciju, poziv Servicea i prikaz rezultata ili pogreške.
   */
  private void registerListeners() {
    frame.vehicles.add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        showAdd();
      }
    });

    frame.vehicles.edit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        showMileageEditor();
      }
    });

    frame.vehicles.activate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        activate();
      }
    });
  }

  /**
   * Dohvaća vozila prijavljenog korisnika i označava aktivno vozilo u tablici.
   *
   * <p>ID trenutačnog aktivnog vozila uzima iz {@link Session}; ako ga nema, prosljeđuje
   * {@code null}. Dohvat i pogreške obrađuje na granici Controller–Service–View.
   */
  public void load() {
    try {
      Integer activeVehicleId = null;
      if (session.getActiveVehicle() != null) {
        activeVehicleId = session.getActiveVehicle().getId();
      }
      frame.vehicles.setRows(vehicleService.list(session.getOwnerId()), activeVehicleId);
    } catch (RuntimeException exception) {
      Ui.error(frame.vehicles, exception);
    }
  }

  /**
   * Vraća trenutno označeno vozilo ili prikazuje poruku ako ništa nije odabrano.
   *
   * @return odabrano vozilo ili {@code null} kada korisnik nije označio vozilo
   */
  private Vehicle selected() {
    Vehicle vehicle = frame.vehicles.selected();
    if (vehicle == null) {
      Ui.info(frame, "Odaberite vozilo.");
    }
    return vehicle;
  }

  /**
   * Otvara i obrađuje dijalog za dodavanje vozila prijavljenog korisnika.
   *
   * <p>Stvara {@link VehicleDialog}, povezuje njegovu {@link VehicleFormController} s katalogom i
   * prije prikaza učitava marke. Listener gumba Spremi zahtijeva odabranu varijantu, zatim predaje
   * vlasnika, varijantu, godinu i kilometražu u {@link VehicleService#add}; nakon uspjeha zatvara
   * dijalog i objavljuje {@link AppEvent#VEHICLE_CHANGED}, čime se osvježavaju ovisni prikazi.
   * Runtime pogreške prikazuje unutar dijaloga, a Odustani ga zatvara bez spremanja.
   */
  private void showAdd() {
    final VehicleDialog dialog = new VehicleDialog(frame);
    VehicleFormController formController = new VehicleFormController(dialog.form, catalogService);
    formController.loadMakes();

    dialog.save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          VehicleVariant variant = dialog.form.selectedVariant();
          if (variant == null) {
            throw new IllegalArgumentException("Odaberite točnu varijantu vozila.");
          }

          vehicleService.add(
              session.getOwnerId(),
              variant.getId(),
              dialog.form.getSelectedYear(),
              dialog.form.getMileage());
          dialog.dispose();
          subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
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

  /**
   * Uređuje kilometražu prethodno odabranog vozila.
   *
   * <p>Ako tablica nema odabran redak, {@link #selected()} prikaže uputu i metoda završava.
   * Inače otvara dijalog s postojećom kilometražom. Spremanje parsira unos kroz {@link Ui#mileage},
   * predaje ga {@link VehicleService#updateMileage}, zatvara dijalog i objavljuje
   * {@link AppEvent#VEHICLE_CHANGED}. Service provodi vlasničku i domensku provjeru; pogreške se
   * prikazuju u dijalogu. Odustajanje ne mijenja vozilo.
   */
  private void showMileageEditor() {
    Vehicle vehicle = selected();
    if (vehicle == null) {
      return;
    }

    final MileageDialog dialog = new MileageDialog(frame, vehicle.getCurrentMileage());

    dialog.save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          vehicleService.updateMileage(session.getOwnerId(), vehicle.getId(), Ui.mileage(dialog.mileage));
          dialog.dispose();
          subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
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

  /**
   * Postavlja označeno vozilo kao aktivno vozilo korisnika i objavljuje promjenu aktivnog konteksta.
   */
  private void activate() {
    Vehicle vehicle = selected();
    if (vehicle == null) {
      return;
    }

    try {
      vehicleService.activate(session.getOwnerId(), vehicle.getId());
      subject.notifyObservers(AppEvent.ACTIVE_VEHICLE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }
}
