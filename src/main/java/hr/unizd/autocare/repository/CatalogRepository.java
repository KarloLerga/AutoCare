package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** JPA dohvat kataloga vozila i standardnih zahvata. */
public class CatalogRepository {
  private final EntityManager entityManager;

  /**
   * Stvara repozitorij za EntityManager trenutačne operacije.
   *
   * @param entityManager EntityManager kojim upravlja pozivajući servis
   */
  public CatalogRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Vraća jedinstvene marke vozila iz kataloga.
   *
   * @return sortirane marke
   */
  public List<String> makes() {
    return entityManager
        .createQuery(
            "select distinct variant.make from VehicleVariant variant order by variant.make",
            String.class)
        .getResultList();
  }

  /**
   * Vraća modele za zadanu marku.
   *
   * @param make marka vozila
   * @return sortirani modeli
   */
  public List<String> models(String make) {
    return entityManager
        .createQuery(
            "select distinct variant.model from VehicleVariant variant "
                + "where variant.make=:make order by variant.model",
            String.class)
        .setParameter("make", make)
        .getResultList();
  }

  /**
   * Sastavlja sortirani popis godina koje pokrivaju varijante marke i modela.
   *
   * @param make marka vozila
   * @param model model vozila
   * @return godine pokrivene kataloškim varijantama
   */
  public List<Integer> years(String make, String model) {
    List<VehicleVariant> variants = entityManager
            .createQuery(
                "select variant from VehicleVariant variant where variant.make=:make "
                    + "and variant.model=:model order by variant.yearFrom,variant.id",
                VehicleVariant.class)
            .setParameter("make", make)
            .setParameter("model", model)
            .getResultList();

    List<Integer> years = new ArrayList<>();
    int currentYear = LocalDate.now().getYear();

    for (VehicleVariant variant : variants) {
      int lastYear = currentYear;
      if (variant.getYearTo() != null && variant.getYearTo() < currentYear) {
        lastYear = variant.getYearTo();
      }

      for (int year = variant.getYearFrom(); year <= lastYear; year++) {
        if (!years.contains(year)) {
          years.add(year);
        }
      }
    }

    Collections.sort(years);
    return years;
  }

  /**
   * Vraća kataloške varijante koje pokrivaju odabranu godinu.
   *
   * @param make marka vozila
   * @param model model vozila
   * @param year godina proizvodnje
   * @return varijante koje pokrivaju godinu
   */
  public List<VehicleVariant> variants(String make, String model, int year) {
    return entityManager
        .createQuery(
            "select variant from VehicleVariant variant where variant.make=:make "
                + "and variant.model=:model and variant.yearFrom<=:year "
                + "and (variant.yearTo is null or variant.yearTo>=:year) "
                + "order by variant.generation,variant.engineLabel,variant.id",
            VehicleVariant.class)
        .setParameter("make", make)
        .setParameter("model", model)
        .setParameter("year", year)
        .getResultList();
  }

  /**
   * Dohvaća varijantu prema primarnom ključu.
   *
   * @param id primarni ključ varijante
   * @return varijanta ili {@code null} ako nije pronađena
   */
  public VehicleVariant findVariant(int id) {
    return entityManager.find(VehicleVariant.class, id);
  }

  /**
   * Vraća standardne radove zadane servisne kategorije.
   *
   * @param category servisna kategorija
   * @return radovi poredani prema nazivu
   */
  public List<WorkDefinition> works(WorkCategory category) {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work where work.category=:category order by work.name",
            WorkDefinition.class)
        .setParameter("category", category)
        .getResultList();
  }

  /**
   * Vraća cijeli informativni katalog standardnih radova.
   *
   * @return radovi poredani prema kategoriji i nazivu
   */
  public List<WorkDefinition> allWorks() {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work order by work.catalogCategory,work.name",
            WorkDefinition.class)
        .getResultList();
  }

  /**
   * Dohvaća standardni rad prema primarnom ključu.
   *
   * @param id primarni ključ rada
   * @return rad ili {@code null} ako nije pronađen
   */
  public WorkDefinition findWork(int id) {
    return entityManager.find(WorkDefinition.class, id);
  }
}
