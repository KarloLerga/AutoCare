package hr.unizd.autocare.controller;

import hr.unizd.autocare.view.components.Ui;
import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** Jednostavan SwingWorker helper: baza radi u pozadini, callback na EDT-u. */
public final class UiTasks {
  public <T> void read(Component parent, Callable<T> work, Consumer<T> success) {
    run(parent, work, success, error -> Ui.error(parent, error));
  }

  public <T> void write(Component parent, Callable<T> work, Consumer<T> success) {
    run(parent, work, success, error -> Ui.error(parent, error));
  }

  public <T> void run(
      Component parent,
      Callable<T> work,
      Consumer<T> success,
      Consumer<Throwable> failure) {
    parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    SwingWorker<T, Void> worker =
        new SwingWorker<>() {
          @Override
          protected T doInBackground() throws Exception {
            return work.call();
          }

          @Override
          protected void done() {
            parent.setCursor(Cursor.getDefaultCursor());
            try {
              success.accept(get());
            } catch (InterruptedException exception) {
              Thread.currentThread().interrupt();
              failure.accept(exception);
            } catch (ExecutionException exception) {
              failure.accept(exception.getCause());
            }
          }
        };
    worker.execute();
  }
}
