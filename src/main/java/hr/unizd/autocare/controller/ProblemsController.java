package hr.unizd.autocare.controller;

import hr.unizd.autocare.app.Session;
import hr.unizd.autocare.service.ProblemService;
import hr.unizd.autocare.view.MainFrame;
import hr.unizd.autocare.view.components.Ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Upravlja pregledom i unosom problema aktivnog vozila.
 *
 * <p>Problem se ovim Controllerom samo evidentira. Rješavanje problema vezano je uz spremanje
 * konkretnog servisa, pa ovdje ne postoji zasebna akcija za ručno zatvaranje problema.
 */
public class ProblemsController {
  /** Glavni prozor koji sadrži prikaz i unos problema aktivnog vozila. */
  private final MainFrame frame;

  /** Dohvaća probleme te sprema nove zapise kroz Service sloj. */
  private final ProblemService problemService;

  /** Daje prijavljenog vlasnika i aktivno vozilo za sva servisna pozivanja. */
  private final Session session;

  /**
   * Povezuje ProblemsView s ProblemServiceom i Sessionom.
   *
   * @param frame glavni prozor
   * @param problemService servis za dohvat i spremanje problema
   * @param session zajednički korisnički kontekst
   */
  public ProblemsController(MainFrame frame, ProblemService problemService, Session session) {
    this.frame = frame;
    this.problemService = problemService;
    this.session = session;

    frame.problems.add.addActionListener(new ActionListener() {
      /** Predaje gumbu za spremanje problema njegovu Controller obradu. */
      @Override
      public void actionPerformed(ActionEvent event) {
        save();
      }
    });
  }

  /**
   * Učitava probleme aktivnog vozila i prikazuje njihovo aktualno stanje.
   *
   * <p>Prosljeđuje ID vlasnika i vozila iz Sessiona ProblemServiceu, a rezultat predaje ProblemsViewu.
   * ProblemService i Repository dodatno ograničavaju dohvat vlasničkim kontekstom. Runtime
   * pogreške prikazuju se uz View i ne prekidaju Swingov event loop.
   */
  public void load() {
    try {
      frame.problems.setRows(problemService.list(session.getOwnerId(), session.getActiveVehicle().getId()));
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }

  /**
   * Sprema novi problem koristeći opis i kategoriju unesenu u ProblemsView.
   *
   * <p>Predaje Serviceu prijavljenog vlasnika, aktivno vozilo i vrijednosti forme. Tek nakon
   * uspješnog spremanja čisti editor i ponovno učitava listu; u slučaju pogreške zadržava unos te
   * prikazuje poruku uz ProblemsView.
   */
  private void save() {
    try {
      problemService.create(
          session.getOwnerId(),
          session.getActiveVehicle().getId(),
          frame.problems.description.getText(),
          frame.problems.selectedCategory());
      frame.problems.clearEditor();
      load();
    } catch (RuntimeException exception) {
      Ui.error(frame.problems, exception);
    }
  }
}
