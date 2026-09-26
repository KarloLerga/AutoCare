package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

/** Čitanje kataloga vozila i standardnih zahvata. */
public class CatalogService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis za čitanje kataloga vozila i standardnih radova.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public CatalogService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Vraća abecedno uređene marke dostupne u katalogu.
   *
   * @return popis marki
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
   * Vraća modele odabrane marke.
   *
   * @param make marka vozila
   * @return modeli marke
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
   * Vraća godine proizvodnje dostupne za marku i model.
   *
   * @param make marka vozila
   * @param model model vozila
   * @return sortirane godine proizvodnje
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
   * Vraća varijante koje odgovaraju marki, modelu i godini.
   *
   * @param make marka vozila
   * @param model model vozila
   * @param year godina proizvodnje
   * @return odgovarajuće varijante
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
   * Vraća radove koji pripadaju zadanoj servisnoj kategoriji.
   *
   * @param category kategorija radova
   * @return radovi u kategoriji
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
   * Vraća sve standardne radove za informativni katalog.
   *
   * @return radovi poredani prema kategoriji kataloga i nazivu
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
