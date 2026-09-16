package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.event.AppEvent;
import hr.unizd.autocare.event.AppEvents;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.util.Arrays;

/** Profil s ponovnom autentikacijom i zastitom nespremljenih promjena. */
public final class ProfileController {
  private final MainFrame frame;
  private final AuthService service;
  private final Session session;
  private final AppEvents events;
  private final UiTasks tasks;
  private Account original;

  public ProfileController(
      MainFrame frame, AuthService service, Session session, AppEvents events, Runnable logout) {
    this.frame = frame;
    this.service = service;
    this.session = session;
    this.events = events;
    tasks = new UiTasks();
    frame.profile.save.addActionListener(e -> save());
    frame.profile.logout.addActionListener(
        e -> {
          if (canLeave()) {
            logout.run();
          }
        });
  }

  public void load() {
    long owner = session.owner();
    tasks.read(
        frame.profile,
        () -> service.account(owner),
        account -> {
          original = account;
          discard();
        });
  }

  public boolean canLeave() {
    if (original == null) {
      return true;
    }
    char[] current = frame.profile.current.getPassword(),
        next = frame.profile.next.getPassword(),
        repeat = frame.profile.repeat.getPassword();
    boolean dirty =
        !original.getName().equals(frame.profile.name.getText())
            || !original.getEmail().equals(frame.profile.email.getText())
            || current.length > 0
            || next.length > 0
            || repeat.length > 0;
    Arrays.fill(current, '\0');
    Arrays.fill(next, '\0');
    Arrays.fill(repeat, '\0');
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
    frame.profile.name.setText(original.getName());
    frame.profile.email.setText(original.getEmail());
    frame.profile.clearPasswords();
  }

  private void save() {
    if (original == null) {
      return;
    }
    String name = frame.profile.name.getText(), email = frame.profile.email.getText();
    char[] current = frame.profile.current.getPassword(),
        next = frame.profile.next.getPassword(),
        repeat = frame.profile.repeat.getPassword();
    if (!Arrays.equals(next, repeat)) {
      Arrays.fill(current, '\0');
      Arrays.fill(next, '\0');
      Arrays.fill(repeat, '\0');
      Ui.info(frame, "Nove lozinke se ne podudaraju.");
      return;
    }
    Arrays.fill(repeat, '\0');
    long owner = session.owner(), version = original.getVersion();
    tasks.write(
        frame,
        () -> {
          try {
            service.profile(owner, version, name, email, current, next);
            return true;
          } finally {
            Arrays.fill(current, '\0');
            Arrays.fill(next, '\0');
          }
        },
        ok -> {
          frame.profile.clearPasswords();
          events.publish(AppEvent.PROFILE_CHANGED);
        });
  }
}
