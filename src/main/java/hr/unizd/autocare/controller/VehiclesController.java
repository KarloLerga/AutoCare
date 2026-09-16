package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Kontroler vozila; service provjerava vlasnika i invarijante. */
public final class VehiclesController {
  private final MainFrame frame;
  private final VehicleService service;
  private final CatalogService catalog;
  private final Session session;
  private final AppEvents events;
  private final UiTasks tasks;

  public VehiclesController(
      MainFrame frame,
      VehicleService service,
      CatalogService catalog,
      Session session,
      AppEvents events) {
    this.frame = frame;
    this.service = service;
    this.catalog = catalog;
    this.session = session;
    this.events = events;
    tasks = new UiTasks(session);
    frame.vehicles.add.addActionListener(e -> edit(null));
    frame.vehicles.edit.addActionListener(
        e -> {
          VehicleRow row = selected();
          if (row != null) {
            edit(row);
          }
        });
    frame.vehicles.activate.addActionListener(
        e -> {
          VehicleRow row = selected();
          if (row != null) {
            tasks.write(
                frame,
                () -> {
                  service.activate(session.owner(), row.getId());
                  return true;
                },
                ok -> events.publish(AppEvent.ACTIVE_VEHICLE_CHANGED));
          }
        });
    frame.vehicles.delete.addActionListener(e -> delete());
  }

  public void load() {
    long owner = session.owner();
    tasks.read(
        frame.vehicles, () -> service.list(owner), rows -> frame.vehicles.table.setRows(rows));
  }

  private VehicleRow selected() {
    VehicleRow row = frame.vehicles.table.selected();
    if (row == null) {
      Ui.info(frame, "Odaberite vozilo.");
    }
    return row;
  }

  private void edit(VehicleRow row) {
    if (row == null) {
      showEditor(null, true);
    } else {
      long owner = session.owner();
      tasks.read(
          frame,
          () -> service.identityEditable(owner, row.getId()),
          editable -> showEditor(row, editable));
    }
  }

  private void showEditor(VehicleRow row, boolean identityEditable) {
    JDialog dialog =
        new JDialog(
            frame,
            row == null ? "Dodaj vozilo" : "Uredi vozilo",
            Dialog.ModalityType.APPLICATION_MODAL);
    VehicleForm form = new VehicleForm();
    VehicleFormController picker = new VehicleFormController(form, catalog, session);
    JButton save = Ui.button("Spremi vozilo", true), cancel = Ui.button("Odustani", false);
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(form);
    root.add(Ui.row(cancel, save), BorderLayout.SOUTH);
    dialog.setContentPane(root);
    dialog.setSize(900, 660);
    dialog.setLocationRelativeTo(frame);
    long owner = session.owner();
    UiTasks editorTask = new UiTasks(session);
    save.addActionListener(
        e -> {
          try {
            VehicleInput input = form.input();
            editorTask.run(
                dialog,
                true,
                () -> {
                  if (row == null) {
                    service.add(owner, input);
                  } else {
                    service.update(owner, row.getId(), row.getVersion(), input);
                  }
                  return true;
                },
                ok -> {
                  dialog.dispose();
                  events.publish(AppEvent.VEHICLE_CHANGED);
                },
                error -> {
                  Ui.error(dialog, error);
                  if (UiTasks.uncertain(error)) {
                    save.setEnabled(false);
                    Ui.info(
                        dialog,
                        "Ne ponavljajte dodavanje naslijepo. Zatvorite obrazac i osvjezite Vozila"
                            + " kako biste provjerili stvarno stanje.");
                  }
                });
          } catch (RuntimeException ex) {
            Ui.error(dialog, ex);
          }
        });
    Runnable close =
        () -> {
          if (!session.isWriting()
              && Ui.confirm(dialog, "Odbaciti nespremljene promjene vozila?")) {
            dialog.dispose();
          }
        };
    cancel.addActionListener(e -> close.run());
    Ui.escape(dialog, close);
    if (row != null) {
      form.existing(row, identityEditable);
    } else {
      SwingUtilities.invokeLater(picker::loadMakes);
    }
    dialog.setVisible(true);
  }

  private void delete() {
    VehicleRow row = selected();
    if (row == null) {
      return;
    }
    List<VehicleRow> rows = frame.vehicles.table.rows();
    if (rows.size() <= 1) {
      Ui.info(frame, "Posljednje vozilo nije moguce obrisati.");
      return;
    }
    Long replacement = null;
    if (row.getActive()) {
      List<VehicleRow> choices = new ArrayList<>();
      for (VehicleRow other : rows) {
        if (other.getId() != row.getId()) {
          choices.add(other);
        }
      }
      String[] labels = new String[choices.size()];
      for (int i = 0; i < labels.length; i++) {
        VehicleRow v = choices.get(i);
        labels[i] =
            v.getVariant().getMake()
                + " "
                + v.getVariant().getModel()
                + " ("
                + v.getYear()
                + ", #"
                + v.getId()
                + ")";
      }
      String answer =
          (String)
              JOptionPane.showInputDialog(
                  frame,
                  "Odaberite novo aktivno vozilo:",
                  "Brisanje aktivnog vozila",
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  labels,
                  labels[0]);
      if (answer == null) {
        return;
      }
      replacement = choices.get(Arrays.asList(labels).indexOf(answer)).getId();
    }
    if (!Ui.confirm(
        frame, "Trajno obrisati vozilo #" + row.getId() + ", njegove servise i probleme?")) {
      return;
    }
    final Long target = replacement;
    long owner = session.owner();
    tasks.write(
        frame,
        () -> {
          service.delete(owner, row.getId(), target);
          return true;
        },
        ok -> events.publish(AppEvent.VEHICLE_CHANGED));
  }
}
