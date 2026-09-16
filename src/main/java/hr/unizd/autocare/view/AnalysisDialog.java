package hr.unizd.autocare.view;
import hr.unizd.autocare.view.components.EstimateFormat;
import javax.swing.*;
import java.awt.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.view.components.*;
/** Preview rezultata odvojen od spremanja problema. */
public final class AnalysisDialog extends JDialog {
    public final JTextArea description=new JTextArea(5, 45);
    public final JButton analyze=Ui.button("Analiziraj", true), save=Ui.button("Spremi problem", true), cancel=Ui.button("Zatvori", false), check=Ui.button("Provjeri spremanje", false);
    public final JLabel estimate=Ui.hint("Rezultat analize jos nije izracunat.");
    public final DataTable<DiagnosticResult> results=new DataTable<>(new String[] {
        "Moguci uzrok / popravak", "Podudaranje %", "Informativna procjena", "Izvor / ogranicenje"
    }, (r, c)->switch(c) {
        case 0->r.getCandidateName();
        case 1->r.getScore();
        case 2->EstimateFormat.display(r.getPrice());
        default->r.getPriceNote();
    });
    public AnalysisDialog(Window owner) {
        super(owner, "Analiza simptoma", ModalityType.APPLICATION_MODAL);
        setSize(900, 680);
        setLocationRelativeTo(owner);
        JPanel root=new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(Ui.BACKGROUND);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JPanel top=Ui.column();
        top.add(Ui.hint("Opisite simptome. Podudaranje pravila nije vjerojatnost niti strucna dijagnoza."));
        top.add(new JScrollPane(description));
        top.add(Ui.row(analyze));
        root.add(top, BorderLayout.NORTH);
        root.add(results);
        JPanel bottom=Ui.column();
        bottom.add(estimate);
        bottom.add(Ui.actions(cancel, check, save));
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
        save.setEnabled(false);
        check.setVisible(false);
    }
}
