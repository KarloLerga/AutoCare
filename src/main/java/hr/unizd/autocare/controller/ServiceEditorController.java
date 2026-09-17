package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Povezuje gumbe servisne forme s njezinim jednostavnim akcijama. */
public final class ServiceEditorController {
  public ServiceEditorController(
      final ServiceEditorDialog view,
      final List<WorkRow> works,
      final ServiceEditorListener listener) {

    view.addMaintenance.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            add(view, works, WorkCategory.MAINTENANCE);
          }
        });

    view.addRepair.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            add(view, works, WorkCategory.REPAIR);
          }
        });

    view.remove.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            removeSelected(view);
          }
        });

    view.save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              ServiceInput serviceInput = view.input();
              listener.saveService(serviceInput);
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

  private static void removeSelected(ServiceEditorDialog view) {
    if (view.itemTable.isEditing() && !view.itemTable.getCellEditor().stopCellEditing()) {
      return;
    }

    int selectedRow = view.itemTable.getSelectedRow();
    if (selectedRow >= 0) {
      int modelRow = view.itemTable.convertRowIndexToModel(selectedRow);
      view.items.remove(modelRow);
    }
  }

  private static void add(
      ServiceEditorDialog view, List<WorkRow> allWorks, WorkCategory category) {
    try {
      List<WorkRow> choices = new ArrayList<>();
      for (WorkRow work : allWorks) {
        if (work.getCategory() == category) {
          choices.add(work);
        }
      }

      WorkRow selectedWork = view.chooseWork(choices);
      if (selectedWork != null) {
        view.items.add(selectedWork);
      }
    } catch (RuntimeException exception) {
      Ui.error(view, exception);
    }
  }
}
