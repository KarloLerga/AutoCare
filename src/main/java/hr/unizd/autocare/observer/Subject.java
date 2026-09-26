package hr.unizd.autocare.observer;

import java.util.ArrayList;
import java.util.List;

public class Subject {
  private final List<Observer> observers = new ArrayList<>();

  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  public void notifyObservers(AppEvent event) {
    for (Observer observer : observers) {
      observer.update(event);
    }
  }
}
