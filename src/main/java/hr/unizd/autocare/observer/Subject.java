package hr.unizd.autocare.observer;

import java.util.ArrayList;
import java.util.List;

/** Čuva Observere i obavještava ih o promjenama stanja aplikacije. */
public class Subject {
  private final List<Observer> observers = new ArrayList<>();

  /** Registrira Observer za buduće događaje. */
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  /** Uklanja Observer iz registriranih primatelja. */
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  /** Šalje događaj svim trenutačno registriranim Observerima. */
  public void notifyObservers(AppEvent event) {
    for (Observer observer : observers) {
      observer.update(event);
    }
  }
}
