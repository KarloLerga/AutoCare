package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Swing forma za izradu novog korisničkog računa. */
public class RegistrationView extends JPanel {
  public final JTextField name = new JTextField(25);
  public final JTextField email = new JTextField(25);
  public final JPasswordField password = new JPasswordField(25);
  public final JPasswordField repeat = new JPasswordField(25);
  public final JButton finish = new JButton("Kreiraj račun");
  public final JButton cancel = new JButton("Odustani");

  /** Stvara polja registracije i gumbe za potvrdu ili odustajanje. */
  public RegistrationView() {
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

  /** Briše oba polja lozinke kako osjetljivi unos ne bi ostao prikazan u formi. */
  public void clearPasswords() {
    password.setText("");
    repeat.setText("");
  }

  /** Vraća registracijsku formu u početno prazno stanje. */
  public void reset() {
    name.setText("");
    email.setText("");
    clearPasswords();
  }
}
