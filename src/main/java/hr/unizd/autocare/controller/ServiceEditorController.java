package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Upravlja unosom stavki u novi servis. */
public final class ServiceEditorController {
  public ServiceEditorController(
      final ServiceEditorDialog view,
      final List<WorkDefinition> works,
      final ServiceEditorListener listener) {
    view.setWorks(new ArrayList<>(works));
    view.type.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            view.filterWorks();
          }
        });
    view.addItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              WorkDefinition selected = view.selectedWork();
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
  }
}
