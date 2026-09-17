package hr.unizd.autocare.event;

import java.util.ArrayList;
import java.util.List;

/** Mali publisher, bez globalnog event busa i bez refleksije. Pozivi na EDT-u. */
public final class AppEvents {
  private final List<AppListener> listeners = new ArrayList<>();

  public void add(AppListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void remove(AppListener listener) {
    listeners.remove(listener);
  }

  public void publish(AppEvent event) {
    for (AppListener listener : listeners) {
      listener.onChange(event);
    }
  }

  public void clear() {
    listeners.clear();
  }
}
