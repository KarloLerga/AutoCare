package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public final class VehicleDialog extends JDialog {
  public final VehicleForm form = new VehicleForm();
  public final JButton save = Ui.button("Spremi vozilo");
  public final JButton cancel = Ui.button("Odustani");

  public VehicleDialog(Window owner) {
    super(owner, "Dodaj vozilo", ModalityType.APPLICATION_MODAL);

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(form, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);

    setContentPane(root);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(820, 440);
    setLocationRelativeTo(owner);
    getRootPane().setDefaultButton(save);
  }
}
