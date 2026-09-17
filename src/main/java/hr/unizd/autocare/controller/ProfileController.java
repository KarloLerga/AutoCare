package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/** Uredjivanje profila i stanje forme prije odjave. */
public final class ProfileController {
  private final MainFrame frame;
  private final AuthService authService;
  private final Session session;
  private final AppEvents events;
  private Account original;

  public ProfileController(
      MainFrame frame, AuthService authService, Session session, AppEvents events) {
    this.frame = frame;
    this.authService = authService;
    this.session = session;
    this.events = events;
    frame.profile.save.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            save();
          }
        });
  }

  public void load() {
    try {
      original = authService.account(session.owner());
      discard();
    } catch (RuntimeException exception) {
      Ui.error(frame.profile, exception);
    }
  }

  public boolean canLeave() {
    if (original == null) {
      return true;
    }
    char[] currentPassword = frame.profile.current.getPassword();
    char[] newPassword = frame.profile.next.getPassword();
    char[] repeatedPassword = frame.profile.repeat.getPassword();
    boolean dirty =
        !original.getName().equals(frame.profile.name.getText())
            || !original.getEmail().equals(frame.profile.email.getText())
            || currentPassword.length > 0
            || newPassword.length > 0
            || repeatedPassword.length > 0;
    clear(currentPassword, newPassword, repeatedPassword);
    if (!dirty) {
      return true;
    }
    if (!Ui.confirm(frame, "Odbaciti nespremljene promjene profila?")) {
      return false;
    }
    discard();
    return true;
  }

  public void clear() {
    original = null;
    frame.profile.name.setText("");
    frame.profile.email.setText("");
    frame.profile.clearPasswords();
  }

  private void discard() {
    if (original == null) {
      return;
    }
    frame.profile.name.setText(original.getName());
    frame.profile.email.setText(original.getEmail());
    frame.profile.clearPasswords();
  }

  private void save() {
    if (original == null) {
      return;
    }
    String name = frame.profile.name.getText();
    String email = frame.profile.email.getText();
    char[] currentPassword = frame.profile.current.getPassword();
    char[] newPassword = frame.profile.next.getPassword();
    char[] repeatedPassword = frame.profile.repeat.getPassword();
    if (!Arrays.equals(newPassword, repeatedPassword)) {
      clear(currentPassword, newPassword, repeatedPassword);
      Ui.info(frame, "Nove lozinke se ne podudaraju.");
      return;
    }
    try {
      authService.profile(
          session.owner(), name, email, currentPassword, newPassword);
      frame.profile.clearPasswords();
      events.publish(AppEvent.PROFILE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame.profile, exception);
    } finally {
      clear(currentPassword, newPassword, repeatedPassword);
    }
  }

  private static void clear(char[]... values) {
    for (char[] value : values) {
      Arrays.fill(value, '\0');
    }
  }
}
