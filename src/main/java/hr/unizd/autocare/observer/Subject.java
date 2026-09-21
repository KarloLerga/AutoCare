package hr.unizd.autocare.observer;

import java.util.ArrayList;
import java.util.List;

/** Subject čuva Observere i obavještava ih o promjenama. */
public final class Subject {
  private final List<Observer> observers = new ArrayList<>();

  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  public void notifyObservers(AppEvent event) {
    for (Observer observer : observers) {
      observer.update(event);
    }
  }
}
