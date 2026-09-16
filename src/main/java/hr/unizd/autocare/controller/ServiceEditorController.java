package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.WorkPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Zajednicki GUI dio editora, upotrebljiv za onboarding i stvarni servis. */
public final class ServiceEditorController {
  public ServiceEditorController(
      ServiceEditorDialog view,
      List<WorkRow> works,
      Session session,
      Consumer<ServiceInput> submit) {
    view.addMaintenance.addActionListener(e -> add(view, works, WorkCategory.MAINTENANCE));
    view.addRepair.addActionListener(e -> add(view, works, WorkCategory.REPAIR));
    view.remove.addActionListener(
        e -> {
          if (view.itemTable.isEditing()) {
            view.itemTable.getCellEditor().stopCellEditing();
          }
          int row = view.itemTable.getSelectedRow();
          if (row >= 0) {
            view.items.remove(view.itemTable.convertRowIndexToModel(row));
          }
        });
    view.save.addActionListener(
        e -> {
          try {
            submit.accept(view.input());
          } catch (RuntimeException ex) {
            Ui.error(view, ex);
          }
        });
    Runnable cancel =
        () -> {
          if (session.isWriting()) {
            Ui.info(view, "Spremanje je u tijeku. Pricekajte rezultat.");
            return;
          }
          if (Ui.confirm(view, "Odbaciti nespremljene podatke ovog servisa?")) {
            view.dispose();
          }
        };
    view.cancel.addActionListener(e -> cancel.run());
    Ui.escape(view, cancel);
  }

  private void add(ServiceEditorDialog view, List<WorkRow> all, WorkCategory category) {
    try {
      List<WorkRow> choices = new ArrayList<>();
      for (WorkRow w : all) {
        if (w.getCategory() == category) {
          choices.add(w);
        }
      }
      WorkRow selected = WorkPicker.choose(view, choices);
      if (selected != null) {
        view.items.add(selected);
      }
    } catch (RuntimeException ex) {
      Ui.error(view, ex);
    }
  }
}
