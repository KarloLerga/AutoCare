package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.RegistrationView;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Prijava i registracija korisničkog računa. */
public final class AuthController {
  public interface LoginListener {
    void loggedIn(int ownerId);
  }

  private final MainFrame frame;
  private final AuthService authService;
  private final LoginListener loginListener;
  private final RegistrationView registrationView;

  public AuthController(
      MainFrame frame, AuthService authService, LoginListener loginListener) {
    this.frame = frame;
    this.authService = authService;
    this.loginListener = loginListener;
    registrationView = frame.registration;

    frame.login.login.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            login();
          }
        });

    frame.login.register.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            openRegistration();
          }
        });

    registrationView.finish.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            finishRegistration();
          }
        });

    registrationView.cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            cancelRegistration();
          }
        });
  }

  private void login() {
    try {
      int ownerId =
          authService.login(
              frame.login.email.getText(), new String(frame.login.password.getPassword()));
      frame.login.password.setText("");
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void openRegistration() {
    registrationView.reset();
    frame.registration();
  }

  private void finishRegistration() {
    try {
      String password = new String(registrationView.password.getPassword());
      Checks.password(password);

      if (!password.equals(new String(registrationView.repeat.getPassword()))) {
        throw new IllegalArgumentException("Lozinke se ne podudaraju.");
      }

      int ownerId =
          authService.register(
              registrationView.name.getText(), registrationView.email.getText(), password);
      registrationView.clearPasswords();
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(registrationView, exception);
    }
  }

  private void cancelRegistration() {
    registrationView.clearPasswords();
    frame.auth();
  }
}
