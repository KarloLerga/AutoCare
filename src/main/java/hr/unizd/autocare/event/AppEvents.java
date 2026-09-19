package hr.unizd.autocare.event;

import java.util.ArrayList;
import java.util.List;

/** Jednostavni Observer za promjene koje utječu na više ekrana. */
public final class AppEvents {
  private final List<AppListener> listeners = new ArrayList<>();

  public void add(AppListener listener) {
    listeners.add(listener);
  }

  public void publish(AppEvent event) {
    for (AppListener listener : listeners) {
      listener.onChange(event);
    }
  }
}
