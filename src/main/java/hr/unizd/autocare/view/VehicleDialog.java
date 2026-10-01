package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import hr.unizd.autocare.view.components.VehicleForm;
import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/** Modalni dijalog za dodavanje novog vozila pomoću zajedničke VehicleForm komponente. */
public class VehicleDialog extends JDialog {
  /** Zajednička forma za izbor marke, modela, godine, varijante i početne kilometraže. */
  public final VehicleForm form = new VehicleForm();

  /** Predaje odabrane vrijednosti Controlleru na spremanje. */
  public final JButton save = new JButton("Spremi vozilo");

  /** Zatvara dijalog bez dodavanja vozila. */
  public final JButton cancel = new JButton("Odustani");

  /**
   * Stvara modalni dijalog i raspoređuje VehicleForm s gumbima za spremanje i odustajanje.
   *
   * @param owner roditeljski prozor
   */
  public VehicleDialog(Window owner) {
    super(owner, "Dodaj vozilo", ModalityType.APPLICATION_MODAL);

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    root.add(form, BorderLayout.CENTER);
    root.add(Ui.actions(cancel, save), BorderLayout.SOUTH);

    setContentPane(root);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(820, 360);
    setLocationRelativeTo(owner);
    getRootPane().setDefaultButton(save);
  }
}
