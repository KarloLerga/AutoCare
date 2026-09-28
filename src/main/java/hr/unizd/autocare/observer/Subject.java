package hr.unizd.autocare.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Izdavač Observer događaja u aplikaciji.
 *
 * <p>Čuva registrirane Observere i omogućuje Controllerima koji naprave promjenu da pošalju
 * događaj bez izravnog poznavanja Controllera koji se nakon toga trebaju osvježiti.
 */
public class Subject {
  /** Observeri koji će sinkrono primiti svaki događaj objavljen ovim Subjectom. */
  private final List<Observer> observers = new ArrayList<>();

  /**
   * Registrira Observer koji treba primati buduće događaje.
   *
   * @param observer observer koji se dodaje
   */
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  /**
   * Uklanja Observer iz popisa primatelja događaja.
   *
   * @param observer observer koji se uklanja
   */
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  /**
   * Šalje isti događaj svim trenutno registriranim Observerima.
   *
   * @param event događaj koji opisuje promjenu stanja
   */
  public void notifyObservers(AppEvent event) {
    for (Observer observer : observers) {
      observer.update(event);
    }
  }
}
