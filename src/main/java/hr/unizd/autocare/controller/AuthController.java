package hr.unizd.autocare.controller;

import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.RegistrationView;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Upravlja korisničkim tokovima prijave i registracije u presentation sloju.
 *
 * <p>Čita vrijednosti iz {@link hr.unizd.autocare.view.LoginView} i
 * {@link RegistrationView}, delegira provjeru vjerodajnica i stvaranje računa u
 * {@link AuthService}, a greške prikazuje preko {@link Ui}. Ne odlučuje kako se korisnički kontekst
 * učitava nakon prijave: uspjeh vraća pozivatelju kroz {@link LoginListener}, koji u glavnoj
 * kompoziciji predaje ID prijavljenog korisnika u {@code MainController}.
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
   * <p>Čuva prozor, Service, callback i RegistrationView, a zatim postavlja listenere za prijavu,
   * otvaranje registracije, potvrdu registracije i odustajanje. Listeneri delegiraju na imenovane
   * metode kako bi čitanje forme, poziv Servicea i obrada grešaka ostali u Controlleru.
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
      /** Predaje akciju gumba za prijavu toku {@link AuthController#login()}. */
      @Override
      public void actionPerformed(ActionEvent event) {
        login();
      }
    });

    frame.login.register.addActionListener(new ActionListener() {
      /** Otvara registracijski prikaz nakon zahtjeva korisnika. */
      @Override
      public void actionPerformed(ActionEvent event) {
        openRegistration();
      }
    });

    registrationView.finish.addActionListener(new ActionListener() {
      /** Pokreće validaciju i spremanje podataka registracije. */
      @Override
      public void actionPerformed(ActionEvent event) {
        finishRegistration();
      }
    });

    registrationView.cancel.addActionListener(new ActionListener() {
      /** Čisti osjetljive vrijednosti registracijske forme i odustaje od registracije. */
      @Override
      public void actionPerformed(ActionEvent event) {
        cancelRegistration();
      }
    });
  }

  /**
   * Pokušava prijaviti korisnika podacima iz LoginViewa.
   *
   * <p>Čita e-mail i lozinku iz LoginViewa i delegira provjeru AuthServiceu. Nakon uspjeha čisti
   * polje lozinke te preko LoginListenera predaje ID MainControlleru, koji otvara korisnički
   * kontekst. Runtime pogrešku iz validacije ili persistence sloja prikazuje uz glavni prozor, a
   * ne prosljeđuje je Swingovom event loopu.
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
   * <p>Prvo uspoređuje lozinku s njezinom potvrdom, a zatim predaje ime, e-mail i lozinku
   * AuthServiceu. Nakon uspješnog spremanja čisti polja lozinke i predaje novi ID kroz isti
   * LoginListener kao i uspješna prijava, pa se novoregistrirani korisnik odmah otvara u
   * aplikacijskom kontekstu. Pogreška se prikazuje uz registracijsku formu.
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
