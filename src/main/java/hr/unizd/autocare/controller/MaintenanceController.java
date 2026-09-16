package hr.unizd.autocare.controller;
import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.EstimateSelection;
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.service.MaintenanceService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.math.BigDecimal;
import java.util.*;
/** Odabir vise redaka racuna samo privremenu procjenu. */
public final class MaintenanceController {
    private final MainFrame frame;
    private final MaintenanceService service;
    private final Session session;
    private final UiTasks tasks;
    public MaintenanceController(MainFrame frame, MaintenanceService service, Session session) {
        this.frame=frame;
        this.service=service;
        this.session=session;
        tasks=new UiTasks(session);
        frame.maintenance.estimate.addActionListener(e-> {
            int[] selected=frame.maintenance.table.table().getSelectedRows();
            if(selected.length==0) {
                Ui.info(frame, "Odaberite jedan ili vise redaka (Ctrl + klik).");
                return;
            }
            List<BigDecimal> prices=new ArrayList<>();
            StringBuilder notes=new StringBuilder();
            Set<String> selectedCodes=new HashSet<>();
            for(int row:selected) {
                MaintenanceRow m=frame.maintenance.table.rows().get(frame.maintenance.table.table().convertRowIndexToModel(row));
                if(!EstimateSelection.conflicts(selectedCodes, m.getWorkCode()).isEmpty()) {
                    Ui.info(frame, "Odabrani paket vec sadrzi dio drugog zahvata. Uklonite preklopljenu stavku."); return;
                }
                selectedCodes.add(m.getWorkCode());
                prices.add(m.getPrice());
                notes.append("\n").append(m.getName()).append(": ").append(EstimateFormat.display(m.getPrice())).append(" / ").append(Objects.toString(m.getPriceNote(), "Nema izvora cijene"));
            }
            Ui.info(frame, "Informativna procjena: "+EstimateFormat.display(CostSummary.of(prices).getKnownTotal())+(CostSummary.of(prices).getUnknownCount()>0?" + stavke bez procjene (zbroj nije potpun)":"")+"\nNista nije spremljeno i intervali nisu promijenjeni."+notes);
        });
    }
    public void load() {
        long owner=session.owner(), vehicle=session.active().getId();
        tasks.read(frame.maintenance, ()->service.list(owner, vehicle), rows-> {
            frame.maintenance.table.setRows(rows);
            frame.maintenance.coverage.setText(rows.isEmpty()?"Katalog nema pravila za varijantu. To ne znaci da odrzavanje nije potrebno.":"Broj pokrivenih radova: "+rows.size()+". Nepoznat rok nije preporuka. Procjene nisu ponude servisa.");
        });
    }
}
