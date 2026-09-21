package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Jednostavna registracija korisničkog računa. */
public final class OnboardingView extends JPanel {
  public final JTextField name = new JTextField(25);
  public final JTextField email = new JTextField(25);
  public final JPasswordField password = new JPasswordField(25);
  public final JPasswordField repeat = new JPasswordField(25);
  public final JButton finish = Ui.button("Kreiraj račun");
  public final JButton cancel = Ui.button("Odustani");

  public OnboardingView() {
    super(new GridBagLayout());

    JPanel card = Ui.card();
    JPanel form = Ui.form();
    card.add(Ui.heading("Registracija"), BorderLayout.NORTH);
    Ui.field(form, 0, "Ime", name);
    Ui.field(form, 1, "E-mail", email);
    Ui.field(form, 2, "Lozinka (najmanje 6 znakova)", password);
    Ui.field(form, 3, "Ponovi lozinku", repeat);
    card.add(form, BorderLayout.CENTER);
    card.add(Ui.actions(cancel, finish), BorderLayout.SOUTH);
    add(card);
  }

  public void clearPasswords() {
    password.setText("");
    repeat.setText("");
  }

  public void reset() {
    name.setText("");
    email.setText("");
    clearPasswords();
  }
}
