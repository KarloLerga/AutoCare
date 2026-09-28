package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Pruža aplikacijskom sloju podatke kataloga vozila i standardnih radova.
 *
 * <p>Service skriva stvarni način dohvaćanja kataloga tako da Controlleri rade s jasnim metodama
 * umjesto s EntityManagerom i JPQL upitima. Svaka metoda otvara EntityManager za svoj dohvat i
 * zatvara ga u {@code finally} bloku.
 */
public class CatalogService {
  /** JPA tvornica, korištena za otvaranje zasebnog EntityManagera za svaki dohvat kataloga. */
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis kataloga.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public CatalogService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Dohvaća marke vozila koje postoje u katalogu.
   *
   * @return sortirani popis dostupnih marki; prazna lista ako nema kataloških varijanti
   */
  public List<String> makes() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).makes();
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća modele dostupne za odabranu marku.
   *
   * @param make marka vozila
   * @return modeli te marke
   */
  public List<String> models(String make) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).models(make);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća godine za koje postoji barem jedna kataloška varijanta odabranog modela.
   *
   * @param make marka vozila
   * @param model model vozila
   * @return dostupne godine proizvodnje
   */
  public List<Integer> years(String make, String model) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).years(make, model);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća varijante vozila koje odgovaraju odabranoj marki, modelu i godini.
   *
   * @param make marka vozila
   * @param model model vozila
   * @param year godina proizvodnje
   * @return odgovarajuće kataloške varijante
   */
  public List<VehicleVariant> variants(String make, String model, int year) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).variants(make, model, year);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća standardne radove određene vrste.
   *
   * @param category održavanje ili popravak
   * @return radovi zadane vrste
   */
  public List<WorkDefinition> works(WorkCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).works(category);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća sve standardne radove koji se prikazuju u informativnom katalogu.
   *
   * @return kataloški radovi redom koji definira Repository upit
   */
  public List<WorkDefinition> catalog() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new CatalogRepository(entityManager).allWorks();
    } finally {
      entityManager.close();
    }
  }
}
