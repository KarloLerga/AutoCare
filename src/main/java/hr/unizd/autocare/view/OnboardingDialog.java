package hr.unizd.autocare.view;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.view.components.*;
/** Racun, prvo vozilo i opcionalni stari servisi su draft do zavrsnog gumba. */
public final class OnboardingDialog extends JDialog {
    public final JTextField name=new JTextField(25), email=new JTextField(25);
    public final JPasswordField password=new JPasswordField(25), repeat=new JPasswordField(25);
    public final VehicleForm vehicle=new VehicleForm();
    public final JButton back=Ui.button("Natrag", false), next=Ui.button("Nastavi", true), finish=Ui.button("Zavrsi registraciju", true), cancel=Ui.button("Odustani", false), addHistory=Ui.button("Dodaj poznati servis", false), removeHistory=Ui.button("Ukloni odabrani servis", false);
    public final DataTable<ServiceInput> history=new DataTable<>(new String[] {
        "Datum", "Km", "Broj stavki", "Napomena"
    }, (s, c)->switch(c) {
        case 0->s.getDate();
        case 1->s.getMileage();
        case 2->s.getItems().size();
        default->s.getNote();
    });
    private final CardLayout cards=new CardLayout();
    private final JPanel body=new JPanel(cards);
    private final JLabel stepLabel=Ui.heading("1 / 3 - Korisnicki racun");
    private int step;
    public OnboardingDialog(Window owner) {
        super(owner, "Registracija - AutoCare", ModalityType.APPLICATION_MODAL);
        setSize(920, 720);
        setLocationRelativeTo(owner);
        JPanel root=new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.add(stepLabel, BorderLayout.NORTH);
        JPanel account=Ui.form();
        Ui.field(account, 0, "Ime", name);
        Ui.field(account, 1, "E-mail", email);
        Ui.field(account, 2, "Lozinka (12 - 128 znakova)", password);
        Ui.field(account, 3, "Ponovi lozinku", repeat);
        body.add(account, "0");
        body.add(vehicle, "1");
        JPanel h=new JPanel(new BorderLayout(8, 8));
        h.add(Ui.row(addHistory, removeHistory), BorderLayout.NORTH);
        h.add(history);
        h.add(Ui.hint("Ovaj korak je opcionalan. Cijena starog servisa smije ostati nepoznata."), BorderLayout.SOUTH);
        body.add(h, "2");
        root.add(body);
        root.add(Ui.row(cancel, back, next, finish), BorderLayout.SOUTH);
        setContentPane(root);
        step(0);
    }
    public int step() {
        return step;
    }
    public void step(int step) {
        this.step=step;
        cards.show(body, Integer.toString(step));
        stepLabel.setText(new String[] {
            "1 / 3 - Korisnicki racun", "2 / 3 - Prvo vozilo", "3 / 3 - Poznata servisna povijest"
        }
        [step]);
        back.setEnabled(step>0);
        next.setVisible(step<2);
        finish.setVisible(step==2);
        getRootPane().setDefaultButton(step==2?finish:next);
    }
    public void clearPasswords() {
        password.setText("");
        repeat.setText("");
    }
}
