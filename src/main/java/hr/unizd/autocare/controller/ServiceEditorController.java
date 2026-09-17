package hr.unizd.autocare.controller;

import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Povezuje inline izbor rada i stvarne cijene sa servisnim zapisom. */
public final class ServiceEditorController {
  public ServiceEditorController(
      final ServiceEditorDialog view,
      final List<WorkRow> works,
      final ServiceEditorListener listener) {
    view.setWorks(new ArrayList<>(works));
    view.addItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              WorkRow selected = view.selectedWork();
              if (selected == null) {
                throw new IllegalArgumentException("Odaberite rad.");
              }
              view.addWork(selected);
            } catch (RuntimeException exception) {
              Ui.error(view, exception);
            }
          }
        });
    view.remove.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            view.removeSelectedItem();
          }
        });
    view.save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              listener.saveService(view.input());
            } catch (RuntimeException exception) {
              Ui.error(view, exception);
            }
          }
        });
    view.cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            view.dispose();
          }
        });
    Ui.escape(view);
  }
}
