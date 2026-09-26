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

/** Učitava i filtrira informativni katalog zahvata. */
public class CatalogController {
  private final MainFrame frame;
  private final CatalogService catalogService;

  public CatalogController(MainFrame frame, CatalogService catalogService) {
    this.frame = frame;
    this.catalogService = catalogService;

    frame.catalog.searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
    frame.catalog.search.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
    frame.catalog.category.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        load();
      }
    });
  }

  public void load() {
    try {
      filterRows(catalogService.catalog());
    } catch (RuntimeException exception) {
      Ui.error(frame.catalog, exception);
    }
  }

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
