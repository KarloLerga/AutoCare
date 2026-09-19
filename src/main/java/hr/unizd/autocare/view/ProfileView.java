package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Osnovni podaci korisničkog profila. */
public final class ProfileView extends JPanel {
  public final JTextField name = new JTextField(25);
  public final JTextField email = new JTextField(25);
  public final JButton save = Ui.button("Spremi promjene");
  public final JButton logout = Ui.button("Odjava");

  public ProfileView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);

    add(Ui.heading("Profil"), BorderLayout.NORTH);

    JPanel form = Ui.form();
    Ui.field(form, 0, "Ime", name);
    Ui.field(form, 1, "E-mail", email);

    JPanel card = Ui.card();
    card.add(form, BorderLayout.NORTH);
    card.add(Ui.row(save, logout), BorderLayout.SOUTH);

    add(card);
  }
}
