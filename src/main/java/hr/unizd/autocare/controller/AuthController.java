package hr.unizd.autocare.controller;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.CatalogService;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.OnboardingDialog;
import hr.unizd.autocare.view.ServiceEditorDialog;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Prijava i trokoracni onboarding bez polovicnog spremanja korisnika. */
public final class AuthController {
  public interface LoginListener {
    void loggedIn(long ownerId);
  }

  private final MainFrame frame;
  private final AuthService authService;
  private final CatalogService catalogService;
  private final LoginListener loginListener;
  private OnboardingDialog onboardingView;
  private VehicleFormController vehiclePicker;
  private List<ServiceInput> registrationHistory;
  private long historyVariantId;
  private boolean vehiclePickerStarted;

  public AuthController(
      MainFrame frame,
      AuthService authService,
      CatalogService catalogService,
      LoginListener loginListener) {
    this.frame = frame;
    this.authService = authService;
    this.catalogService = catalogService;
    this.loginListener = loginListener;
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
  }

  private void login() {
    String email = frame.login.email.getText();
    char[] password = frame.login.password.getPassword();
    try {
      Account account = authService.login(email, password);
      frame.login.password.setText("");
      loginListener.loggedIn(account.getId());
    } catch (RuntimeException exception) {
      Ui.error(frame, exception);
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  private void openRegistration() {
    onboardingView = new OnboardingDialog(frame);
    vehiclePicker = new VehicleFormController(onboardingView.vehicle, catalogService);
    registrationHistory = new ArrayList<>();
    historyVariantId = -1L;
    vehiclePickerStarted = false;

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
    Ui.escape(onboardingView);
    onboardingView.setVisible(true);
  }

  private void nextRegistrationStep() {
    try {
      if (onboardingView.step() == 0) {
        validateAccountStep();
      } else {
        VehicleInput vehicleInput = onboardingView.vehicle.input();
        if (!registrationHistory.isEmpty() && historyVariantId != vehicleInput.getVariantId()) {
          boolean discard =
              Ui.confirm(
                  onboardingView,
                  "Promijenili ste varijantu. Odbaciti povijest prethodne varijante?");
          if (!discard) {
            return;
          }
          registrationHistory.clear();
          onboardingView.setHistory(registrationHistory);
          historyVariantId = -1L;
        }
      }
      int nextStep = onboardingView.step() + 1;
      onboardingView.step(nextStep);
      if (nextStep == 1 && !vehiclePickerStarted) {
        vehiclePickerStarted = true;
        vehiclePicker.loadMakes();
      }
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
    char[] password = onboardingView.password.getPassword();
    char[] repeatedPassword = onboardingView.repeat.getPassword();
    try {
      Checks.password(password);
      if (!Arrays.equals(password, repeatedPassword)) {
        throw new IllegalArgumentException("Lozinke se ne podudaraju.");
      }
    } finally {
      Arrays.fill(password, '\0');
      Arrays.fill(repeatedPassword, '\0');
    }
  }

  private void addHistoryItem() {
    try {
      VehicleInput vehicleInput = onboardingView.vehicle.input();
      int currentMileage = vehicleInput.getMileage();
      long variantId = vehicleInput.getVariantId();
      List<WorkRow> works =
          new ArrayList<>(catalogService.onboardingWorks(variantId, WorkCategory.MAINTENANCE));
      works.addAll(catalogService.onboardingWorks(variantId, WorkCategory.REPAIR));
      ServiceEditorDialog editor =
          new ServiceEditorDialog(onboardingView, currentMileage, true, new ArrayList<>());
      new ServiceEditorController(
          editor,
          works,
          new ServiceEditorListener() {
            @Override
            public void saveService(ServiceInput serviceInput) {
              ServiceRecordService.validate(serviceInput, true);
              if (serviceInput.getMileage() > currentMileage) {
                throw new IllegalArgumentException(
                    "Povijest ne moze imati vise km od trenutnog stanja.");
              }
              historyVariantId = variantId;
              registrationHistory.add(serviceInput);
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
    ServiceInput selectedService = onboardingView.selectedHistory();
    if (selectedService != null) {
      registrationHistory.remove(selectedService);
      onboardingView.setHistory(registrationHistory);
      if (registrationHistory.isEmpty()) {
        historyVariantId = -1L;
      }
    }
  }

  private void finishRegistration() {
    try {
      String name = onboardingView.name.getText();
      String email = onboardingView.email.getText();
      VehicleInput vehicleInput = onboardingView.vehicle.input();
      if (!registrationHistory.isEmpty() && historyVariantId != vehicleInput.getVariantId()) {
        throw new IllegalArgumentException(
            "Povijest je za prethodnu varijantu; vratite se i provjerite odabir.");
      }
      char[] password = onboardingView.password.getPassword();
      long ownerId;
      try {
        ownerId =
            authService.register(
                name, email, password, vehicleInput, new ArrayList<>(registrationHistory));
      } finally {
        Arrays.fill(password, '\0');
      }
      onboardingView.clearPasswords();
      onboardingView.dispose();
      loginListener.loggedIn(ownerId);
    } catch (RuntimeException exception) {
      Ui.error(onboardingView, exception);
    }
  }

  private void cancelRegistration() {
    if (Ui.confirm(
        onboardingView, "Odustati od registracije? Nespremljeni podaci bit ce odbaceni.")) {
      onboardingView.clearPasswords();
      onboardingView.dispose();
    }
  }
}
