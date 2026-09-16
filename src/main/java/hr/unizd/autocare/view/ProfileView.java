package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Osjetljive promjene zahtijevaju trenutnu lozinku. */
public final class ProfileView extends JPanel {
  public final JTextField name = new JTextField(25), email = new JTextField(25);
  public final JPasswordField current = new JPasswordField(25),
      next = new JPasswordField(25),
      repeat = new JPasswordField(25);
  public final JButton save = Ui.button("Spremi promjene", true),
      logout = Ui.button("Odjava", false);

  public ProfileView() {
    super(new BorderLayout(12, 12));
    setOpaque(false);
    add(Ui.heading("Profil"), BorderLayout.NORTH);
    JPanel form = Ui.form();
    Ui.field(form, 0, "Ime", name);
    Ui.field(form, 1, "E-mail", email);
    Ui.field(form, 2, "Trenutna lozinka", current);
    Ui.field(form, 3, "Nova lozinka (opcionalno)", next);
    Ui.field(form, 4, "Ponovi novu lozinku", repeat);
    JPanel card = Ui.card();
    card.add(form, BorderLayout.NORTH);
    card.add(Ui.row(save, logout), BorderLayout.SOUTH);
    add(card);
  }

  public void clearPasswords() {
    current.setText("");
    next.setText("");
    repeat.setText("");
  }
}
