package hr.unizd.autocare.view.components;
import hr.unizd.autocare.model.Data.*;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.List;
/** Picker identiteta; nema SQL-a. Godina -> marka -> model -> filtrirane varijante. */
public final class VehicleForm extends JPanel {
    public final JSpinner year=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1886, LocalDate.now().getYear(), 1));
    public final JComboBox<String> make=new JComboBox<>(), model=new JComboBox<>();
    public final JTextField search=new JTextField(18);
    public final JButton find=Ui.button("Pretrazi varijante", false);
    public final JSpinner mileage=Ui.mileage(0);
    public final JLabel state=Ui.hint("Odaberite godinu, marku, model i tocnu varijantu.");
    public final DataTable<VariantRow> variants=new DataTable<>(new String[] {
        "Generacija", "Motor", "Gorivo", "Mjenjac", "KS", "Od", "Do"
    }, (v, c)->switch(c) {
        case 0->v.getGeneration();
        case 1->v.getEngine();
        case 2->v.getFuel();
        case 3->v.getTransmission();
        case 4->v.getPowerHp();
        case 5->v.getFrom();
        default->v.getTo();
    });
    public boolean updating;
    public VehicleForm() {
        super(new BorderLayout(12, 12));
        setOpaque(false);
        year.setEditor(new JSpinner.NumberEditor(year, "0"));
        JPanel f=Ui.form();
        Ui.field(f, 0, "Godina proizvodnje", year);
        Ui.field(f, 1, "Marka", make);
        Ui.field(f, 2, "Model", model);
        Ui.field(f, 3, "Motor ili generacija", Ui.row(search, find));
        Ui.field(f, 4, "Trenutna kilometraza", mileage);
        add(f, BorderLayout.NORTH);
        variants.setPreferredSize(new Dimension(700, 210));
        add(variants, BorderLayout.CENTER);
        add(state, BorderLayout.SOUTH);
        make.setMaximumRowCount(18);
        model.setMaximumRowCount(18);
    }
    public VehicleInput input() {
        VariantRow v=variants.selected();
        if(v==null)throw new IllegalArgumentException("Odaberite tocnu varijantu u tablici.");
        return new VehicleInput(v.getId(), Ui.integer(year), Ui.integer(mileage));
    }
    public void existing(VehicleRow vehicle, boolean identityEditable) {
        updating=true;
        year.setValue(vehicle.getYear());
        make.setModel(new DefaultComboBoxModel<>(new String[] {
            vehicle.getVariant().getMake()
        }));
        model.setModel(new DefaultComboBoxModel<>(new String[] {
            vehicle.getVariant().getModel()
        }));
        variants.setRows(List.of(vehicle.getVariant()));
        variants.table().setRowSelectionInterval(0, 0);
        mileage.setValue(vehicle.getMileage());
        year.setEnabled(identityEditable);
        make.setEnabled(identityEditable);
        model.setEnabled(identityEditable);
        search.setEnabled(identityEditable);
        find.setEnabled(identityEditable);
        state.setText(identityEditable?"Za promjenu identiteta prvo ponovno odaberite godinu i katalog.":"Identitet je zakljucan jer vozilo ima povijest; kilometraza se moze povecati.");
        updating=false;
    }
}
