package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.OnboardingView;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Prijava i trokorakna registracija unutar istog JFramea. */
public final class AuthController {
  public interface LoginListener {
    void loggedIn(long ownerId);
  }

  private final MainFrame frame;
  private final AuthService authService;
  private final CatalogService catalogService;
  private final LoginListener loginListener;
  private final OnboardingView onboardingView;
  private final VehicleFormController vehiclePicker;
  private final List<ServiceInput> registrationHistory = new ArrayList<>();
  private long historyVariantId = -1L;

  public AuthController(
      MainFrame frame,
      AuthService authService,
      CatalogService catalogService,
      LoginListener loginListener) {
    this.frame = frame;
    this.authService = authService;
    this.catalogService = catalogService;
    this.loginListener = loginListener;
    onboardingView = frame.onboarding;
    vehiclePicker = new VehicleFormController(onboardingView.vehicle, catalogService);

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
    onboardingView.next.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            nextRegistrationStep();
          }
        });
    onboardingView.back.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            previousRegistrationStep();
          }
        });
    onboardingView.addHistory.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            addHistoryItem();
          }
        });
    onboardingView.removeHistory.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            removeHistoryItem();
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
          authService.login(frame.login.email.getText(), new String(frame.login.password.getPassword()));
      frame.login.password.setText("");
      loginListener.loggedIn(account.getId());
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    }
  }

  private void openRegistration() {
    onboardingView.reset();
    registrationHistory.clear();
    historyVariantId = -1L;
    frame.registration();
    vehiclePicker.loadMakes();
  }

  private void nextRegistrationStep() {
    try {
      if (onboardingView.step() == 0) {
        validateAccountStep();
      } else if (onboardingView.step() == 1) {
        VehicleInput input = onboardingView.vehicle.input();
        if (!registrationHistory.isEmpty() && historyVariantId != input.getVariantId()) {
          throw new IllegalArgumentException(
              "Povijest je za drugu varijantu; uklonite je ili vratite odabranu varijantu.");
        }
      }
      onboardingView.step(onboardingView.step() + 1);
    } catch (RuntimeException exception) {
      Ui.error(onboardingView, exception);
    }
  }

  private void previousRegistrationStep() {
    if (onboardingView.step() > 0) {
      onboardingView.step(onboardingView.step() - 1);
    }
  }

  private void validateAccountStep() {
    Checks.text(onboardingView.name.getText(), 100, "Ime");
    Checks.email(onboardingView.email.getText());
    String password = new String(onboardingView.password.getPassword());
    Checks.password(password);
    if (!password.equals(new String(onboardingView.repeat.getPassword()))) {
      throw new IllegalArgumentException("Lozinke se ne podudaraju.");
    }
  }

  private void addHistoryItem() {
    try {
      VehicleInput vehicleInput = onboardingView.vehicle.input();
      List<WorkDefinition> works =
          new ArrayList<>(
              catalogService.works(WorkCategory.MAINTENANCE));
      works.addAll(
          catalogService.works(WorkCategory.REPAIR));
      ServiceEditorDialog editor =
          new ServiceEditorDialog(frame, vehicleInput.getMileage(), true, List.of());
      new ServiceEditorController(
          editor,
          works,
          new ServiceEditorListener() {
            @Override
            public void saveService(ServiceInput serviceInput) {
              ServiceRecordService.validate(serviceInput, true);
              if (serviceInput.getMileage() > vehicleInput.getMileage()) {
                throw new IllegalArgumentException(
                    "Povijest ne može imati veću kilometražu od trenutnog stanja.");
              }
              registrationHistory.add(serviceInput);
              historyVariantId = vehicleInput.getVariantId();
              onboardingView.setHistory(registrationHistory);
              editor.dispose();
            }
          });
      editor.setVisible(true);
    } catch (RuntimeException exception) {
      Ui.error(onboardingView, exception);
    }
  }

  private void removeHistoryItem() {
    ServiceInput selected = onboardingView.selectedHistory();
    if (selected != null) {
      registrationHistory.remove(selected);
      onboardingView.setHistory(registrationHistory);
      if (registrationHistory.isEmpty()) {
        historyVariantId = -1L;
      }
    }
  }

  private void finishRegistration() {
    try {
      VehicleInput input = onboardingView.vehicle.input();
      if (!registrationHistory.isEmpty() && historyVariantId != input.getVariantId()) {
        throw new IllegalArgumentException("Povijest je za drugu varijantu vozila.");
      }
      long ownerId =
          authService.register(
              onboardingView.name.getText(),
              onboardingView.email.getText(),
              new String(onboardingView.password.getPassword()),
              input,
              new ArrayList<>(registrationHistory));
      onboardingView.clearPasswords();
      loginListener.loggedIn(ownerId);
      frame.application();
    } catch (RuntimeException exception) {
      Ui.error(onboardingView, exception);
    }
  }

  private void cancelRegistration() {
    onboardingView.clearPasswords();
    frame.auth();
  }
}
