package hr.unizd.autocare.service;

import hr.unizd.autocare.repository.Repositories;
import java.util.function.Function;

/**
 * Tehnicki izvrsava transakciju koju je svojom granicom odredio Service. Service odabire sve korake
 * callbacka; repositoryji samo pristupaju podacima. Implementacija svakom pozivu daje nov
 * EntityManager i isti kontekst svim repositoryjima.
 */
public interface TransactionRunner {

  /**
   * Izvrsava kratku operaciju citanja. Callback ne smije mijenjati managed entitete. Read je ugovor
   * aplikacije, a ne SQL zabrana pisanja ni jamstvo snapshot izolacije.
   *
   * @param action citanje i priprema rezultata dok je persistence context otvoren
   * @param <T> obican rezultat koji ne sadrzi JPA proxy
   * @return rezultat nakon uspjesnog zavrsetka transakcije
   */
  <T> T read(Function<Repositories, T> action);

  /**
   * Izvrsava cijelu poslovnu promjenu u jednoj transakciji. Callback ne smije otvarati novu
   * transakciju, cekati GUI ni objaviti dogadjaj uspjeha. Novi zapisi, promjene kilometraze i
   * zatvaranje problema uspijevaju zajedno.
   *
   * @param action svi koraci koje Service zeli spremiti atomarno
   * @param <T> identifikator/potvrda, ne zivi managed entitet
   * @return rezultat tek nakon uspjesnog commita
   * @throws RuntimeException ako transakcija ili poslovna operacija nije uspjela
   */
  <T> T write(Function<Repositories, T> action);
}
