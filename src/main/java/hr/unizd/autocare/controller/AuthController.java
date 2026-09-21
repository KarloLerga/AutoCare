package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.OnboardingView;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Prijava i registracija korisničkog računa. */
public final class AuthController {
  public interface LoginListener {
    void loggedIn(long ownerId);
  }

  private final MainFrame frame;
  private final AuthService authService;
  private final LoginListener loginListener;
  private final OnboardingView onboardingView;

  public AuthController(
      MainFrame frame, AuthService authService, LoginListener loginListener) {
    this.frame = frame;
    this.authService = authService;
    this.loginListener = loginListener;
    onboardingView = frame.onboarding;

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

    onboardingView.finish.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            finishRegistration();
          }
        });

    onboardingView.cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            cancelRegistration();
          }
        });
  }

  private void login() {
    try {
      Account account =
          authService.login(
              frame.login.email.getText(), new String(frame.login.password.getPassword()));
      frame.login.password.setText("");
      loginListener.loggedIn(account.getId());
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void openRegistration() {
    onboardingView.reset();
    frame.registration();
  }

  private void finishRegistration() {
    try {
      String password = new String(onboardingView.password.getPassword());
      Checks.password(password);

      if (!password.equals(new String(onboardingView.repeat.getPassword()))) {
        throw new IllegalArgumentException("Lozinke se ne podudaraju.");
      }

      long ownerId =
          authService.register(
              onboardingView.name.getText(), onboardingView.email.getText(), password);
      onboardingView.clearPasswords();
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(onboardingView, exception);
    }
  }

  private void cancelRegistration() {
    onboardingView.clearPasswords();
    frame.auth();
  }
}
