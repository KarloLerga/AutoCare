package hr.unizd.autocare.controller;
import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.service.*;
import hr.unizd.autocare.view.*;
import hr.unizd.autocare.view.components.*;
import javax.swing.*;
import java.time.Clock;
import java.util.*;
import java.util.function.LongConsumer;
/** Prijava i trokoracni onboarding bez polovicnog spremanja korisnika. */
public final class AuthController {
    private final MainFrame frame;
    private final AuthService auth;
    private final CatalogService catalog;
    private final Session session;
    private final UiTasks tasks;
    private final LongConsumer entered;
    public AuthController(MainFrame frame, AuthService auth, CatalogService catalog, Session session, LongConsumer entered) {
        this.frame=frame;
        this.auth=auth;
        this.catalog=catalog;
        this.session=session;
        this.entered=entered;
        tasks=new UiTasks(session);
        frame.login.login.addActionListener(e->login());
        frame.login.register.addActionListener(e->register());
    }
    private void login() {
        String email=frame.login.email.getText();
        char[] password=frame.login.password.getPassword();
        tasks.read(frame, ()-> {
            try {
                return auth.login(email, password);
            }
            finally {
                Arrays.fill(password, '\0');
            }
        }, account-> {
            frame.login.password.setText("");
            entered.accept(account.getId());
        });
    }
    private void register() {
        tasks.invalidate(); // Odbaci prethodni kasni login rezultat pri otvaranju drugog toka.
        OnboardingDialog view=new OnboardingDialog(frame);
        List<ServiceInput> history=new ArrayList<>();
        long[] historyVariant={-1L};
        VehicleFormController picker=new VehicleFormController(view.vehicle, catalog, session);
        boolean[] pickerStarted= {
            false
        };
        UiTasks wizardTasks=new UiTasks(session);
        view.next.addActionListener(e-> {
            try {
                if(view.step()==0) {
                    Checks.text(view.name.getText(), 100, "Ime");
                    Checks.email(view.email.getText());
                    char[] a=view.password.getPassword(), b=view.repeat.getPassword();
                    try {
                        Checks.password(a);
                        if(!Arrays.equals(a, b))throw new IllegalArgumentException("Lozinke se ne podudaraju.");
                    }
                    finally {
                        Arrays.fill(a, '\0');
                        Arrays.fill(b, '\0');
                    }
                }
                else {
                    VehicleInput now=view.vehicle.input();
                    if(!history.isEmpty() && historyVariant[0]!=now.getVariantId()) {
                        if(!Ui.confirm(view, "Promijenili ste varijantu. Odbaciti povijest prethodne varijante?"))return;
                        history.clear(); view.history.setRows(history); historyVariant[0]=-1L;
                    }
                }
                view.step(view.step()+1);
                if(view.step()==1&&!pickerStarted[0]) {
                    pickerStarted[0]=true;
                    picker.loadMakes();
                }
            }
            catch(RuntimeException ex) {
                Ui.error(view, ex);
            }
        });
        view.back.addActionListener(e->view.step(view.step()-1));
        view.addHistory.addActionListener(e-> {
            int km;
            long variant;
            try {
                VehicleInput selected=view.vehicle.input();
                km=selected.getMileage(); variant=selected.getVariantId();
            }
            catch(RuntimeException ex) {
                Ui.error(view, ex);
                return;
            }
            wizardTasks.read(view, ()-> {
                List<WorkRow> w=new ArrayList<>(catalog.onboardingWorks(variant, WorkCategory.MAINTENANCE));
                w.addAll(catalog.onboardingWorks(variant, WorkCategory.REPAIR));
                return w;
            }, works-> {
                ServiceEditorDialog editor=new ServiceEditorDialog(view, km, true, List.of());
                new ServiceEditorController(editor, works, session, input-> {
                    ServiceRecordService.validate(input, true, Clock.systemDefaultZone());
                    if(input.getMileage()>km)throw new IllegalArgumentException("Povijest ne moze imati vise km od trenutnog stanja.");
                    historyVariant[0]=variant;
                    history.add(input);
                    view.history.setRows(history);
                    editor.dispose();
                });
                editor.setVisible(true);
            });
        });
        view.removeHistory.addActionListener(e-> {
            ServiceInput selected=view.history.selected();
            if(selected!=null) {
                history.remove(selected);
                view.history.setRows(history);
            }
        });
        view.finish.addActionListener(e-> {
            String name=view.name.getText(), email=view.email.getText();
            VehicleInput vehicle;
            try {
                vehicle=view.vehicle.input();
                if(!history.isEmpty()&&historyVariant[0]!=vehicle.getVariantId())throw new IllegalArgumentException("Povijest je za prethodnu varijantu; vratite se i provjerite odabir.");
            }
            catch(RuntimeException ex) {
                Ui.error(view, ex);
                return;
            }
            char[] password=view.password.getPassword();
            List<ServiceInput> snapshot=List.copyOf(history);
            wizardTasks.run(view, true, ()-> {
                try {
                    return auth.register(name, email, password, vehicle, snapshot);
                }
                finally {
                    Arrays.fill(password, '\0');
                }
            }, id-> {
                view.clearPasswords();
                view.dispose();
                entered.accept(id);
            }, error-> {
                Ui.error(view, error);
                if(UiTasks.uncertain(error)) {
                    view.finish.setEnabled(false);
                    Ui.info(view, "Registracija mozda postoji. Zatvorite ovaj obrazac i pokusajte prijavu istim e-mailom; ne kreirajte drugi racun.");
                }
            });
        });
        Runnable cancel=()-> {
            if(session.isWriting()) {
                Ui.info(view, "Pricekajte zavrsetak registracije.");
                return;
            }
            if(Ui.confirm(view, "Odustati od registracije? Nespremljeni podaci bit ce odbaceni.")) {
                view.clearPasswords();
                view.dispose();
            }
        };
        view.cancel.addActionListener(e->cancel.run());
        Ui.escape(view, cancel);
        view.setVisible(true);
    }
}
