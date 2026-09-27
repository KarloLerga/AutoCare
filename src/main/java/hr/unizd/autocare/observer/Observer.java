package hr.unizd.autocare.observer;

/**
 * Sudionik Observer obrasca koji prima obavijest nakon promjene važnog stanja aplikacije.
 */
public interface Observer {
  /**
   * Reagira na događaj koji je objavio Subject.
   *
   * @param event događaj koji opisuje što se u aplikaciji promijenilo
   */
  void update(AppEvent event);
}
