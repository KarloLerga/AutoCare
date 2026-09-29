package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Upravlja prikazom i filtriranjem informativnog kataloga standardnih radova.
 *
 * <p>Service dohvati katalog, a ovaj Controller primjenjuje tekstualni i kategorijski filter prije
 * predaje redaka Viewu; filtriranje se zato odvija u aplikacijskom sloju, ne u upitu Repositoryja.
 */
public class CatalogController {
  /** Glavni prozor koji sadrži CatalogView i prikazuje eventualne pogreške. */
  private final MainFrame frame;

  /** Dohvaća kataloške definicije standardnih radova. */
  private final CatalogService catalogService;

  /**
   * Povezuje CatalogView s CatalogServiceom i registrira akciju pretraživanja.
   *
   * @param frame glavni prozor koji sadrži CatalogView
   * @param catalogService servis za dohvat kataloških radova
   */
  public CatalogController(MainFrame frame, CatalogService catalogService) {
    this.frame = frame;
    this.catalogService = catalogService;

    frame.catalog.searchButton.addActionListener(new ActionListener() {
      /** Ponovno dohvaća i filtrira katalog nakon klika na gumb za pretraživanje. */
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
    frame.catalog.search.addActionListener(new ActionListener() {
      /** Pokreće isto pretraživanje kada korisnik potvrdi unos tipkovnicom. */
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
    frame.catalog.category.addActionListener(new ActionListener() {
      /** Primjenjuje novi kategorijski filtar na prikazane radove. */
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
  }

  /** Dohvaća cijeli katalog i primjenjuje trenutačni tekstualni i kategorijski filter Viewa. */
  public void load() {
    try {
      filterRows(catalogService.catalog());
    } catch (RuntimeException exception) {
      Ui.error(frame.catalog, exception);
    }
  }

  /**
   * Filtrira dohvaćene radove prema tekstu pretrage i odabranoj kategoriji.
   *
   * <p>Tekst iz polja pretvara u prazan niz ako nedostaje, zatim uklanja rubne razmake i uspoređuje
   * nazive bez obzira na velika i mala slova. Prolazi sve dohvaćene radove, preskače one izvan
   * odabrane kategorije ili one čiji naziv ne sadrži pretraživani tekst, a preostale predaje
   * CatalogViewu. Prazan tekst ili kategorija bez odabira ne ograničavaju odgovarajući filtar.
   *
   * @param allWorks svi radovi dohvaćeni iz kataloga
   */
  private void filterRows(List<WorkDefinition> allWorks) {
    String search = frame.catalog.search.getText();
    if (search == null) {
      search = "";
    }
    search = search.trim().toLowerCase();

    CatalogCategory selectedCategory = frame.catalog.selectedCategory();
    List<WorkDefinition> filteredWorks = new ArrayList<>();

    for (WorkDefinition work : allWorks) {
      if (selectedCategory != null && work.getCatalogCategory() != selectedCategory) {
        continue;
      }
      if (!search.isEmpty() && !work.getName().toLowerCase().contains(search)) {
        continue;
      }
      filteredWorks.add(work);
    }

    frame.catalog.setRows(filteredWorks);
  }
}
