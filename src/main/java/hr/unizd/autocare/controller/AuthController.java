package hr.unizd.autocare.controller;

import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.RegistrationView;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Upravlja korisničkim tokovima prijave i registracije.
 *
 * <p>Controller čita vrijednosti iz Login i Registration Viewova, poziva {@code AuthService},
 * prikazuje validacijske pogreške preko zajedničkog UI helpera te nakon uspješne prijave javlja
 * {@code LoginListeneru} identifikator prijavljenog korisnika.
 */
public class AuthController {
  /** Callback kojim AuthController obavještava ostatak aplikacije da je prijava završila uspješno. */
  public interface LoginListener {
    /**
     * Prima identifikator upravo prijavljenog korisnika.
     *
     * @param ownerId identifikator prijavljenog korisnika
     */
    void loggedIn(int ownerId);
  }

  /** Glavni prozor s LoginViewom i RegistrationViewom kojima ovaj Controller povezuje akcije. */
  private final MainFrame frame;

  /** Provodi provjere vjerodajnica i use-case registracije izvan GUI sloja. */
  private final AuthService authService;

  /** Obavještava pozivatelja, najčešće MainController, nakon uspješne prijave. */
  private final LoginListener loginListener;

  /** Registracijski prikaz čiji unos i akcije obrađuje ovaj Controller. */
  private final RegistrationView registrationView;

  /**
   * Stvara Controller za prijavu i registraciju te povezuje akcije odgovarajućih Viewova.
   *
   * @param frame glavni prozor koji sadrži login i registration prikaze
   * @param authService servis koji provodi prijavu i registraciju
   * @param loginListener callback koji preuzima kontrolu nakon uspješne prijave
   */
  public AuthController(MainFrame frame, AuthService authService, LoginListener loginListener) {
    this.frame = frame;
    this.authService = authService;
    this.loginListener = loginListener;
    registrationView = frame.registration;

    frame.login.login.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        login();
      }
    });

    frame.login.register.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        openRegistration();
      }
    });

    registrationView.finish.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        finishRegistration();
      }
    });

    registrationView.cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        cancelRegistration();
      }
    });
  }

  /**
   * Pokušava prijaviti korisnika podacima iz LoginViewa.
   *
   * <p>Lozinku čita neposredno prije poziva Servicea. Nakon uspješne prijave čisti polje lozinke
   * i prosljeđuje ID korisnika LoginListeneru. Validacijska ili persistence pogreška prikazuje se
   * korisniku bez rušenja aplikacije.
   */
  private void login() {
    try {
      int ownerId = authService.login(frame.login.email.getText(), new String(frame.login.password.getPassword()));
      frame.login.password.setText("");
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  /** Priprema praznu registracijsku formu i prebacuje glavni prozor na registraciju. */
  private void openRegistration() {
    registrationView.reset();
    frame.registration();
  }

  /**
   * Provjerava podatke registracijske forme i stvara novi korisnički račun.
   *
   * <p>Controller dodatno provjerava podudaranje unesene i ponovljene lozinke, zatim poziva
   * AuthService. Nakon uspješne registracije vraća korisnika na prijavu.
   */
  private void finishRegistration() {
    try {
      String password = new String(registrationView.password.getPassword());
      if (!password.equals(new String(registrationView.repeat.getPassword()))) {
        throw new IllegalArgumentException("Lozinke se ne podudaraju.");
      }

      int ownerId = authService.register(registrationView.name.getText(), registrationView.email.getText(), password);
      registrationView.clearPasswords();
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(registrationView, exception);
    }
  }

  /** Odustaje od registracije, briše osjetljive vrijednosti forme i vraća prikaz prijave. */
  private void cancelRegistration() {
    registrationView.clearPasswords();
    frame.auth();
  }
}
