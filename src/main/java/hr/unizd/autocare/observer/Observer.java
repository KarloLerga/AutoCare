package hr.unizd.autocare.observer;

/** Observer prima obavijest kada se promijeni stanje aplikacije. */
public interface Observer {
  /** Reagira na događaj koji je objavio Subject. */
  void update(AppEvent event);
}
