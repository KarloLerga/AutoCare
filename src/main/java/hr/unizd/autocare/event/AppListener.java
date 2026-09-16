package hr.unizd.autocare.event;
/** Observer ugovor, isti jednostavni registriraj-obavijesti princip kao na vjezbama. */
public interface AppListener {
    void onChange(AppEvent event);
}
