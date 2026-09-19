package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Učitava pretraživi informativni katalog za aktivno vozilo. */
public final class CatalogController {
  private final MainFrame frame;
  private final CatalogService catalogService;
  private final Session session;

  public CatalogController(MainFrame frame, CatalogService catalogService, Session session) {
    this.frame = frame;
    this.catalogService = catalogService;
    this.session = session;

    frame.catalog.searchButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            load();
          }
        });
    frame.catalog.search.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            load();
          }
        });
    frame.catalog.category.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            load();
          }
        });
  }

  public void load() {
    if (session.getActiveVehicle() == null) {
      return;
    }
    try {
      frame.catalog.setRows(
          catalogService.catalog(
              session.getOwnerId(),
              session.getActiveVehicle().getId(),
              frame.catalog.search.getText(),
              frame.catalog.selectedCategory()));
    } catch (RuntimeException exception) {
      Ui.error(frame.catalog, exception);
    }
  }
}
