package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Swing forma za unos vjerodajnica postojećeg korisnika i prelazak na registraciju. */
public class LoginView extends JPanel {
  /** E-mail adresa koju AuthController prosljeđuje AuthServiceu. */
  public final JTextField email = new JTextField(25);

  /** Lozinka unesena za prijavu; Controller je nakon pokušaja prijave briše. */
  public final JPasswordField password = new JPasswordField(25);

  /** Pokreće obradu prijave koju registrira AuthController. */
  public final JButton login = new JButton("Prijavi se");

  /** Otvara registracijski prikaz. */
  public final JButton register = new JButton("Kreiraj račun");

  /** Stvara i raspoređuje polja e-maila, lozinke i akcijske gumbe prijave. */
  public LoginView() {
    super(new GridBagLayout());
    JPanel card = Ui.card(), form = Ui.form();
    card.add(Ui.heading("AutoCare"), BorderLayout.NORTH);
    Ui.field(form, 0, "E-mail", email);
    Ui.field(form, 1, "Lozinka", password);
    card.add(form, BorderLayout.CENTER);
    card.add(Ui.row(login, register), BorderLayout.SOUTH);
    add(card);
  }
}
