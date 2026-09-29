package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/**
 * Persistentna definicija standardnog rada iz kataloga.
 *
 * <p>Rad ima vrstu, korisničku kategoriju, opcionalni kilometarski i vremenski interval te
 * informativni raspon cijene. Raspon cijene nije stvarno plaćeni iznos korisnikova servisa.
 */
@Entity
public class WorkDefinition {
  /** Identifikator kataloške definicije rada koji dodjeljuje baza. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /** Naziv standardnog rada prikazan korisniku. */
  private String name;

  /** Razlikuje planirano održavanje od popravka; {@code STRING} pohranjuje naziv enum vrijednosti. */
  @Enumerated(EnumType.STRING)
  private WorkCategory category;

  /** Kategorija koja grupira rad u katalogu; {@code STRING} pohranjuje naziv enum vrijednosti. */
  @Enumerated(EnumType.STRING)
  private CatalogCategory catalogCategory;

  /** Kilometarski interval održavanja ili {@code null} ako rad nema takav interval. */
  private Integer intervalKm;

  /** Vremenski interval održavanja u mjesecima ili {@code null} ako se ne koristi. */
  private Integer intervalMonths;

  /** Donja granica informativnog raspona cijene, ne stvarni trošak servisa. */
  private BigDecimal minPrice;

  /** Gornja granica informativnog raspona cijene, ne stvarni trošak servisa. */
  private BigDecimal maxPrice;

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected WorkDefinition() {}

  /** Dohvaća bazni identifikator koji povezuje rad s njegovim referencama u katalogu i servisima.
   *
   * @return identifikator kataloške definicije rada
   */
  public Integer getId() {
    return id;
  }

  /** Dohvaća naziv rada koji se prikazuje u katalogu i povijesti servisa.
   *
   * @return naziv standardnog rada
   */
  public String getName() {
    return name;
  }

  /** Dohvaća razvrstavanje rada na održavanje ili popravak.
   *
   * @return vrsta rada, odnosno održavanje ili popravak
   */
  public WorkCategory getCategory() {
    return category;
  }

  /** Dohvaća kategoriju kojom se rad grupira u korisničkom katalogu.
   *
   * @return korisnička kategorija kataloga
   */
  public CatalogCategory getCatalogCategory() {
    return catalogCategory;
  }

  /** Dohvaća preporučeni kilometarski razmak između izvedbi ovog rada.
   *
   * @return kilometarski servisni interval ili {@code null} ako nije zadan
   */
  public Integer getIntervalKm() {
    return intervalKm;
  }

  /** Dohvaća preporučeni vremenski razmak između izvedbi ovog rada.
   *
   * @return vremenski servisni interval u mjesecima ili {@code null} ako nije zadan
   */
  public Integer getIntervalMonths() {
    return intervalMonths;
  }

  /** Dohvaća donju granicu informativne cijene iz kataloga, ne stvarni trošak servisa.
   *
   * @return informativna najniža cijena ili {@code null} ako nije navedena
   */
  public BigDecimal getMinPrice() {
    return minPrice;
  }

  /** Dohvaća gornju granicu informativne cijene iz kataloga, ne stvarni trošak servisa.
   *
   * @return informativna najviša cijena ili {@code null} ako nije navedena
   */
  public BigDecimal getMaxPrice() {
    return maxPrice;
  }

  /** Vraća naziv rada kako bi se objekt mogao prikazati u tekstualnom izboru.
   *
   * @return naziv rada prikladan za tekstualni prikaz
   */
  @Override
  public String toString() {
    return name;
  }
}
