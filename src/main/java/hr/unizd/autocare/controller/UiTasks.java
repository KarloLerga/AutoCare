package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.service.AppException;
import hr.unizd.autocare.view.components.Ui;
import java.awt.Component;
import java.awt.Cursor;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/** Mali SwingWorker omotac: pozadina za IO, done za GUI, epoch/ticket za zastarjele rezultate. */
public final class UiTasks {
  private static final Logger LOG = Logger.getLogger(UiTasks.class.getName());
  private final Session session;
  private long sequence;

  public UiTasks(Session session) {
    this.session = session;
  }

  public void invalidate() {
    sequence++;
  }

  public <T> void read(Component parent, Callable<T> work, Consumer<T> success) {
    run(parent, false, work, success, error -> Ui.error(parent, error));
  }

  public <T> void write(Component parent, Callable<T> work, Consumer<T> success) {
    run(parent, true, work, success, error -> Ui.error(parent, error));
  }

  public <T> void run(
      Component parent,
      boolean write,
      Callable<T> work,
      Consumer<T> success,
      Consumer<Throwable> failure) {
    if (session.isWriting()) {
      Ui.info(parent, "Pricekajte zavrsetak spremanja.");
      return;
    }
    final long epoch = session.epoch(), ticket = ++sequence;
    final Map<Component, Boolean> enabled = write ? Ui.disableTree(parent) : Map.of();
    if (write) {
      session.setWriting(true);
    }
    parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new SwingWorker<T, Void>() {
      @Override
      protected T doInBackground() throws Exception {
        return work.call();
      }

      @Override
      protected void done() {
        if (write) {
          session.setWriting(false);
          Ui.restore(enabled);
        }
        parent.setCursor(Cursor.getDefaultCursor());
        T result;
        try {
          result = get();
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          failure.accept(ex);
          return;
        } catch (ExecutionException ex) {
          Throwable cause = ex.getCause();
          LOG.log(Level.WARNING, "AutoCare operacija nije uspjela", cause);
          if (epoch == session.epoch() && ticket == sequence && parent.isDisplayable()) {
            failure.accept(cause);
          }
          return;
        } catch (CancellationException ex) {
          return;
        }
        if (epoch != session.epoch() || ticket != sequence || !parent.isDisplayable()) {
          return;
        }
        try {
          success.accept(result);
        } catch (RuntimeException ex) {
          LOG.log(
              Level.SEVERE,
              write
                  ? "Write je potvrden; naknadni GUI callback nije uspio."
                  : "GUI callback nije uspio.",
              ex);
          Ui.info(
              parent,
              write
                  ? "Promjena je spremljena, ali prikaz nije osvjezen. Otvorite ekran ponovno."
                  : "Podaci su ucitani, ali prikaz nije uspio. Osvjezite ekran.");
        }
      }
    }.execute();
  }

  public static boolean uncertain(Throwable error) {
    return error instanceof AppException e && e.getKind() == AppException.Kind.COMMIT_UNKNOWN;
  }
}
