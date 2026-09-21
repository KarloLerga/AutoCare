package hr.unizd.autocare.controller;

import hr.unizd.autocare.observer.AppEvent;
import hr.unizd.autocare.observer.Subject;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.VehicleService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import hr.unizd.autocare.app.Session;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Dodavanje, aktiviranje, brisanje i promjena kilometraže vozila. */
public final class VehiclesController {
  private final MainFrame frame;
  private final VehicleService vehicleService;
  private final CatalogService catalogService;
  private final Session session;
  private final Subject subject;

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

  private void registerListeners() {
    frame.vehicles.add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            showAdd();
          }
        });
    frame.vehicles.edit.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            showMileageEditor();
          }
        });
    frame.vehicles.activate.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            activate();
          }
        });
    frame.vehicles.delete.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            delete();
          }
        });
  }

  public void load() {
    try {
      frame.vehicles.setRows(vehicleService.list(session.getOwnerId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.vehicles, exception);
    }
  }

  private VehicleRow selected() {
    VehicleRow vehicle = frame.vehicles.selected();
    if (vehicle == null) {
      Ui.info(frame, "Odaberite vozilo.");
    }
    return vehicle;
  }

  private void showAdd() {
    JDialog dialog = new JDialog(frame, "Dodaj vozilo", Dialog.ModalityType.APPLICATION_MODAL);
    VehicleForm form = new VehicleForm();
    new VehicleFormController(form, catalogService).loadMakes();
    JButton save = Ui.button("Spremi vozilo");
    JButton cancel = Ui.button("Odustani");
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(form, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);
    dialog.setContentPane(root);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(820, 440);
    dialog.setLocationRelativeTo(frame);
    save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              vehicleService.add(session.getOwnerId(), form.input());
              dialog.dispose();
              subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
            } catch (RuntimeException exception) {
              Ui.error(dialog, exception);
            }
          }
        });
    cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            dialog.dispose();
          }
        });
    dialog.setVisible(true);
  }

  private void showMileageEditor() {
    VehicleRow vehicle = selected();
    if (vehicle == null) {
      return;
    }
    JDialog dialog =
        new JDialog(frame, "Promijeni kilometražu", Dialog.ModalityType.APPLICATION_MODAL);
    JTextField mileage = new JTextField(Integer.toString(vehicle.getMileage()), 14);
    JLabel hint = Ui.hint("Kilometraža se može samo povećati.");
    JButton save = Ui.button("Spremi");
    JButton cancel = Ui.button("Odustani");
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    JPanel form = Ui.form();
    Ui.field(form, 0, "Nova kilometraža", mileage);
    form.add(hint);
    root.add(form, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);
    dialog.setContentPane(root);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.pack();
    dialog.setLocationRelativeTo(frame);
    save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            try {
              vehicleService.updateMileage(
                  session.getOwnerId(), vehicle.getId(), Ui.mileage(mileage));
              dialog.dispose();
              subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
            } catch (RuntimeException exception) {
              Ui.error(dialog, exception);
            }
          }
        });
    cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            dialog.dispose();
          }
        });
    dialog.setVisible(true);
  }

  private void activate() {
    VehicleRow vehicle = selected();
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

  private void delete() {
    VehicleRow vehicle = selected();
    if (vehicle == null) {
      return;
    }
    if (!Ui.confirm(frame, "Trajno obrisati vozilo, njegove servise i probleme?")) {
      return;
    }
    try {
      vehicleService.delete(session.getOwnerId(), vehicle.getId());
      subject.notifyObservers(AppEvent.VEHICLE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }
}
