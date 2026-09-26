package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MileageDialog extends JDialog {
  public final JTextField mileage;
  public final JButton save = new JButton("Spremi");
  public final JButton cancel = new JButton("Odustani");

  public MileageDialog(Window owner, int currentMileage) {
    super(owner, "Promijeni kilometražu", ModalityType.APPLICATION_MODAL);
    mileage = new JTextField(Integer.toString(currentMileage), 14);

    JPanel form = Ui.form();
    Ui.field(form, 0, "Nova kilometraža", mileage);

    JPanel center = Ui.column();
    center.add(form);
    center.add(Ui.hint("Kilometraža se može samo povećati."));

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(center, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);

    setContentPane(root);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack();
    setLocationRelativeTo(owner);
    getRootPane().setDefaultButton(save);
  }
}
