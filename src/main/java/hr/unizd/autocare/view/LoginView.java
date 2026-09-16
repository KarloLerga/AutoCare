package hr.unizd.autocare.view;

import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Prijava bez sidebara. */
public final class LoginView extends JPanel {
  public final JTextField email = new JTextField(25);
  public final JPasswordField password = new JPasswordField(25);
  public final JButton login = Ui.button("Prijavi se", true),
      register = Ui.button("Kreiraj racun", false);

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
