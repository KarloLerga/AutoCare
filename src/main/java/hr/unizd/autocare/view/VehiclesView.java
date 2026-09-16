package hr.unizd.autocare.view;
import javax.swing.*;
import java.awt.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.view.components.*;
/** Upravljanje vozilima; jedino mjesto promjene aktivnog vozila. */
public final class VehiclesView extends JPanel {
    public final JButton add=Ui.button("Dodaj vozilo", true), edit=Ui.button("Uredi", false), activate=Ui.button("Aktiviraj", false), delete=Ui.button("Obrisi", false);
    public final DataTable<VehicleRow> table=new DataTable<>(new String[] {
        "Vozilo", "Godina", "Motor", "Kilometraza", "Aktivno"
    }, (v, c)->switch(c) {
        case 0->v.getVariant().getMake()+" "+v.getVariant().getModel();
        case 1->v.getYear();
        case 2->v.getVariant().getEngine();
        case 3->v.getMileage();
        default->v.getActive()?"Da":"";
    });
    public VehiclesView() {
        super(new BorderLayout(12, 12));
        setOpaque(false);
        JPanel top=Ui.column();
        top.add(Ui.heading("Vozila"));
        top.add(Ui.row(add, edit, activate, delete));
        add(top, BorderLayout.NORTH);
        add(table);
        add(Ui.hint("Brisanje uklanja i servisnu povijest i probleme odabranog vozila."), BorderLayout.SOUTH);
    }
}
