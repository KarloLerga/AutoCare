package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Povezuje događaje u {@link VehicleForm} s dohvatom kataloških opcija.
 *
 * <p>Kontroler vodi korisnika kroz kaskadu marka, model, godina i varijanta. Nakon promjene
 * višeg izbora očisti ovisne niže odabire pa ih ponovno učita preko {@link CatalogService}; time
 * forma prikazuje samo kombinacije koje postoje u katalogu.
 */
public class VehicleFormController {
  /** Swing forma čije ovisne odabire ovaj Controller učitava iz kataloga. */
  private final VehicleForm view;

  /** Dohvaća marke, modele, godine i varijante za uzastopne izbore u formi. */
  private final CatalogService catalogService;
  /**
   * Označuje programsko čišćenje ili punjenje ComboBoxova. Takve izmjene mogu pokrenuti Swing
   * ActionListenere kao i korisnikov odabir; listeneri zato ignoriraju događaje dok je ova zastavica
   * postavljena, čime se sprječavaju nepotrebna kaskadna dohvaćanja tijekom rekonfiguracije forme.
   */
  private boolean updating;

  /**
   * Povezuje formu sa servisom i registrira listenere za kaskadne odabire.
   *
   * <p>Promjena marke učitava modele, promjena modela učitava godine, a promjena godine učitava
   * varijante. Odabir varijante prikazuje njezine detalje. Svaki listener provjerava {@code updating}
   * kako programsko punjenje ili resetiranje ComboBoxa ne bi pokrenulo sljedeći korak kao da je
   * vrijednost izabrao korisnik.
   *
   * @param view forma za izbor vozila
   * @param catalogService servis iz kojeg se dohvaćaju kataloške vrijednosti
   */
  public VehicleFormController(VehicleForm view, CatalogService catalogService) {
    this.view = view;
    this.catalogService = catalogService;

    view.make.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadModels();
        }
      }
    });

    view.model.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadYears();
        }
      }
    });

    view.year.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          loadVariants();
        }
      }
    });

    view.variant.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (!updating) {
          view.showDetails(view.selectedVariant());
        }
      }
    });
  }

  /**
   * Učitava marke iz kataloga i vraća sve ovisne odabire na početno stanje.
   *
   * <p>Dok View čisti i puni ComboBoxove, {@code updating} sprječava aktiviranje lančanih
   * listenera. Nakon punjenja metoda prikaže uputu za odabir marke ili poruku da katalog nema
   * dostupnih marki. Runtime pogreške prikazuje kroz zajednički {@link Ui#error} mehanizam i
   * vraća zastavicu u normalno stanje.
   */
  public void loadMakes() {
    try {
      view.state.setText("Učitavanje marki...");
      List<String> values = catalogService.makes();

      updating = true;
      view.reset();
      view.setMakes(values);
      updating = false;

      if (values.isEmpty()) {
        view.state.setText("Nema kataloga za odabir.");
      } else {
        view.state.setText("Odaberite marku.");
      }
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /**
   * Učitava modele za trenutačno odabranu marku i priprema formu za sljedeći odabir.
   *
   * <p>Najprije čita marku iz Viewa i programski čisti model, godinu i varijantu jer postojeći
   * izbori možda ne pripadaju novoj marki. Ako marka nedostaje, postavlja odgovarajuću uputu i
   * završava. Inače dohvaća modele preko {@link CatalogService}, puni ComboBox dok je
   * {@code updating} postavljen te traži od korisnika da odabere model. Time se sprečava da Swingovi
   * događaji nastali punjenjem forme odmah pokrenu učitavanje godina. Runtime pogrešku prikazuje
   * preko {@link Ui#error} i vraća zastavicu u normalno stanje.
   */
  private void loadModels() {
    String make = (String) view.make.getSelectedItem();

    updating = true;
    view.clearBelowMake();
    updating = false;

    if (make == null) {
      view.state.setText("Odaberite marku.");
      return;
    }

    try {
      view.state.setText("Učitavanje modela...");
      List<String> values = catalogService.models(make);

      updating = true;
      view.setModels(values);
      updating = false;

      view.state.setText("Odaberite model.");
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /**
   * Učitava dostupne godine za odabranu marku i model te priprema varijantu za novi izbor.
   *
   * <p>Prije dohvaćanja čisti godinu i varijantu jer više ne predstavljaju valjan nastavak novog
   * modela. Ako marka ili model nisu odabrani, prikazuje uputu i završava. Inače traži godine u
   * {@link CatalogService} i programski puni njihov ComboBox uz potisnute ActionListenere. Pogreške
   * prikazuje kroz {@link Ui#error}.
   */
  private void loadYears() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();

    updating = true;
    view.clearBelowModel();
    updating = false;

    if (make == null || model == null) {
      view.state.setText("Odaberite model.");
      return;
    }

    try {
      view.state.setText("Učitavanje godina...");
      List<Integer> values = catalogService.years(make, model);

      updating = true;
      view.setYears(values);
      updating = false;

      view.state.setText("Odaberite godinu proizvodnje.");
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /**
   * Učitava kataloške varijante koje odgovaraju odabranoj marki, modelu i godini.
   *
   * <p>Najprije uklanja prethodnu varijantu jer ona možda ne odgovara novoj godini. Ako nedostaje
   * marka, model ili godina, postavlja uputu i završava. Inače dohvaća odgovarajuće entitete preko
   * {@link CatalogService}, puni izbornik bez reagiranja na programske događaje te prikazuje ili
   * da nema rezultata ili da korisnik treba odabrati točnu varijantu. Detalji se prikazuju tek kada
   * korisnik izabere varijantu. Runtime pogreške prikazuje kroz {@link Ui#error}.
   */
  private void loadVariants() {
    String make = (String) view.make.getSelectedItem();
    String model = (String) view.model.getSelectedItem();
    Integer year = (Integer) view.year.getSelectedItem();

    updating = true;
    view.clearBelowYear();
    updating = false;

    if (make == null || model == null || year == null) {
      view.state.setText("Odaberite godinu proizvodnje.");
      return;
    }

    try {
      view.state.setText("Učitavanje varijanti...");
      List<VehicleVariant> values = catalogService.variants(make, model, year);

      updating = true;
      view.setVariants(values);
      updating = false;

      if (values.isEmpty()) {
        view.state.setText("Nema varijanti za odabranu godinu.");
      } else {
        view.state.setText("Odaberite točnu varijantu.");
      }
    } catch (RuntimeException exception) {
      updating = false;
      Ui.error(view, exception);
    }
  }

  /**
   * Čisti formu bez pokretanja njezinih ovisnih listenera tijekom programskog resetiranja.
   *
   * <p>Ova metoda samo resetira View; ponovno učitavanje marki obavlja {@link #loadMakes()} kada
   * ga pozivatelj izričito zatraži.
   */
  public void reset() {
    updating = true;
    view.reset();
    updating = false;
  }
}
