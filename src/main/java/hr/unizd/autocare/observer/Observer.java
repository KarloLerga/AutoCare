package hr.unizd.autocare.observer;

/** Observer prima obavijest kada se promijeni stanje aplikacije. */
public interface Observer {
  void update(AppEvent event);
}
