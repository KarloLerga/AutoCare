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

/** Uredjivanje osnovnih podataka profila. */
public final class ProfileController {
  private final MainFrame frame;
  private final AuthService authService;
  private final Session session;
  private final AppEvents events;
  private Account original;

  public ProfileController(
      MainFrame frame,
      AuthService authService,
      Session session,
      AppEvents events) {
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
      frame.profile.name.setText(original.getName());
      frame.profile.email.setText(original.getEmail());
    } catch (RuntimeException exception) {
      Ui.error(frame.profile, exception);
    }
  }

  public boolean canLeave() {
    return true;
  }

  public void clear() {
    original = null;
    frame.profile.name.setText("");
    frame.profile.email.setText("");
  }

  private void save() {
    if (original == null) {
      return;
    }

    try {
      authService.profile(
          session.owner(),
          frame.profile.name.getText(),
          frame.profile.email.getText());

      events.publish(AppEvent.PROFILE_CHANGED);
    } catch (RuntimeException exception) {
      Ui.error(frame.profile, exception);
    }
  }
}
