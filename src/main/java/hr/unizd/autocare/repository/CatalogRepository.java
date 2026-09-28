package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Repository za čitanje kataloga varijanti vozila i standardnih radova.
 *
 * <p>Kataloški podaci u aplikaciji služe kao referentni skup iz kojeg korisnički tokovi biraju
 * postojeće marke, modele, varijante i radove. Klasa sadrži JPQL i dohvat po primarnom ključu,
 * ali ne otvara EntityManager ni ne koordinira transakcije.
 */
public class CatalogRepository {
  /** EntityManager koji Service sloj otvara za trenutačni katalog use-case. */
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
   * Dohvaća opcije marki iz kataloških varijanti vozila.
   *
   * <p>Upit vraća samo scalar {@code String} vrijednosti, uklanja ponavljanja jer više varijanti
   * može pripadati istoj marki te sortira nazive za prikaz u prvom koraku {@code VehicleForm}.
   * Cijeli {@code VehicleVariant} entiteti u ovom koraku nisu potrebni.
   *
   * @return jedinstvene marke vozila sortirane za prikaz
   */
  public List<String> makes() {
    return entityManager
        .createQuery(
            "select distinct variant.make from VehicleVariant variant order by variant.make",
            String.class)
        .getResultList();
  }

  /**
   * Dohvaća modele koji postoje u katalogu za odabranu marku.
   *
   * <p>JPQL filtrira varijante prema marki, vraća različite nazive modela i sortira ih. Rezultat
   * je lista naziva, a ne entiteta, jer je formi u ovom koraku potreban samo sljedeći izbor u
   * kaskadi marka, model, godina i varijanta.
   *
   * @param make marka prema kojoj se filtrira katalog
   * @return jedinstveni nazivi modela odabrane marke, sortirani za prikaz
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
   * Iz raspona godina kataloških varijanti izvodi godine dostupne za odabrani model.
   *
   * <p>Najprije dohvaća odgovarajuće varijante, a zatim proširuje svaki njihov raspon do godine
   * završetka ili tekuće godine ako je raspon otvoren. Rasponi različitih varijanti mogu se
   * preklapati; {@link TreeSet} uklanja tako nastale duplikate i održava prirodni sortirani
   * poredak. Na kraju se vrijednosti vraćaju kao {@code List}, kako bi ih postojeći View mogao
   * prikazati kao opcije.
   *
   * @param make marka vozila
   * @param model model vozila
   * @return sortirane godine koje korisnik može odabrati, bez ponavljanja
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

    Set<Integer> years = new TreeSet<>();
    int currentYear = LocalDate.now().getYear();

    for (VehicleVariant variant : variants) {
      int lastYear = currentYear;
      if (variant.getYearTo() != null && variant.getYearTo() < currentYear) {
        lastYear = variant.getYearTo();
      }

      for (int year = variant.getYearFrom(); year <= lastYear; year++) {
        years.add(year);
      }
    }

    return new ArrayList<>(years);
  }

  /**
   * Dohvaća entitete varijanti koje pokrivaju zadanu marku, model i godinu.
   *
   * <p>JPQL filtrira po marki i modelu te zahtijeva da {@code yearFrom} ne bude kasniji od
   * odabrane godine, a {@code yearTo} bude prazan ili ne raniji od nje. Vraća cijele
   * {@link VehicleVariant} entitete jer sljedeći UI korak treba i motor, gorivo, snagu i mjenjač;
   * rezultati su stabilno poredani po generaciji, oznaci motora i ID-u.
   *
   * @param make marka vozila
   * @param model model vozila
   * @param year odabrana godina
   * @return varijante čiji raspon godina uključuje zadanu godinu, sortirane po generaciji,
   *         oznaci motora i identifikatoru
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
   * Dohvaća jednu katalošku varijantu po primarnom ključu.
   *
   * @param id identifikator varijante
   * @return kataloška varijanta ili {@code null} ako ne postoji
   */
  public VehicleVariant findVariant(int id) {
    return entityManager.find(VehicleVariant.class, id);
  }

  /**
   * Dohvaća radove određene vrste, primjerice samo radove održavanja.
   *
   * @param category vrsta rada
   * @return definicije radova sortirane po nazivu; prazna lista ako ih nema
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
   * Dohvaća cijeli informativni katalog sortiran po grupi i nazivu rada.
   *
   * @return sve definicije radova koje čine informativni katalog
   */
  public List<WorkDefinition> allWorks() {
    return entityManager
        .createQuery(
            "select work from WorkDefinition work order by work.catalogCategory,work.name",
            WorkDefinition.class)
        .getResultList();
  }

  /**
   * Dohvaća jednu katalošku definiciju rada po primarnom ključu.
   *
   * @param id identifikator standardnog rada
   * @return definicija rada ili {@code null} ako ne postoji
   */
  public WorkDefinition findWork(int id) {
    return entityManager.find(WorkDefinition.class, id);
  }
}
