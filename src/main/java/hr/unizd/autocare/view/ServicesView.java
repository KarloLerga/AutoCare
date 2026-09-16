package hr.unizd.autocare.view;

import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.view.components.DataTable;
import hr.unizd.autocare.view.components.Ui;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Servisna povijest aktivnog vozila. */
public final class ServicesView extends JPanel {

    public final JButton add = Ui.button("Novi servis", true);
    public final JButton detail = Ui.button("Detalj", false);

    public final DataTable<ServiceRow> table =
            new DataTable<>(
                    new String[] {
                        "Datum",
                        "Km",
                        "Radovi",
                        "Poznati trosak",
                        "Napomena"
                    },
                    (service, column) -> switch (column) {
                        case 0 -> service.getDate();
                        case 1 -> service.getMileage();
                        case 2 -> service.getNames();
                        case 3 -> Ui.total(service.getTotal());
                        default -> service.getNote();
                    });

    public ServicesView() {
        super(new BorderLayout(12, 12));
        setOpaque(false);

        JPanel top = Ui.column();
        top.add(Ui.heading("Servisna povijest"));
        top.add(Ui.row(add, detail));

        add(top, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }
}
