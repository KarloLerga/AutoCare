# Konacna pravila imenovanja i JPA ugovor

## 1. Odluka, ne univerzalni standard

Za ovaj novi studentski projekt biramo PascalCase tablice i camelCase osnovne stupce koji su jednaki
Java imenima. I snake_case bi bio valjan timski izbor; ne tvrdimo da je ovaj stil opcenito jedini najbolji.
Ovdje smanjuje eksplicitne razlike bez dodatne naming strategije ili nestandardnih Java snake_case polja.

Standardna Hibernate fizicka strategija zadrzava logicka imena. Ne postoji Spring Boot snake-case
konfiguracija koja se moze pretpostaviti u ovom obicnom JPA projektu. Provjeri da lokalno nije uveden
dodatni override. Izvore pogledaj u SOURCES.md (JPA Column/Table/JoinColumn i Hibernate naming).

### Razlika izmedu scalar polja i veze

```java
// Scalar polje: stupac se zove imagePath, standardna nullable/duljina odgovara ugovoru.
private String imagePath;

// Isti naziv currentMileage, ali NOT NULL je namjerno ogranicenje i ostaje vidljivo.
@Column(nullable = false)
private int currentMileage;

// Isti naziv actualPrice, ali preciznost decimalnog iznosa ne treba prepustiti provider defaultu.
@Column(precision = 9, scale = 2)
private BigDecimal actualPrice;

// Veza: objekt u Javi, FK variant_id u bazi. Nema duplog Long variantId polja.
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(nullable = false)
private VehicleVariant variant;
```

`@Id` ne ukljucuje automatski sve ostale postavke i Java sama ne stvara bazu.
`@Column` bez name uzima naziv polja, ali anotacija jos moze opisivati druge namjerne osobine.
JPA `optional=false` moze sluziti provideru da izvede NOT NULL; referenca radi sigurnosti zadrzava
JoinColumn(nullable=false) dok stvarni provider/schema test ne dokaze da je to redundantno.

Jednake nazive s razlicitim velikim/malim slovima ne treba koristiti kao dvije tablice na SQL Serveru.
Promjena `vehicle` u `Vehicle` je kozmeticka case-only promjena; parser/DB kolacija moraju biti provjereni.
Za `User` ne uvodimo posebni quoted keyword: domenska klasa i tablica postaju `AppUser`.

## 2. Tablice

| Java prije | Java poslije / SQL tablica u dbo | SQL prije |
|---|---|---|
| User | AppUser | app_user |
| VehicleVariant | VehicleVariant | vehicle_variant |
| Vehicle | Vehicle | vehicle |
| WorkDefinition | WorkDefinition | work_definition |
| VehicleWorkRule | VehicleWorkRule | vehicle_work_rule |
| ServiceRecord | ServiceRecord | service_record |
| ServiceItem | ServiceItem | service_item |
| Problem | Problem | problem |
| DiagnosticRule | DiagnosticRule | diagnostic_rule |

## 3. Potpuni inventory persistentnih stupaca

Tipovi i nullability uzeti su iz pregledanih Java anotacija, ne iz live Azure metapodataka.
NVARCHAR duljine, DECIMAL, DATE/DATETIME2/BIT i identity treba potvrditi stvarnim Hibernate SQL DDL-om.
Popis ne ukljucuje inverse `ServiceRecord.items`, jer nema svoj stupac u ServiceRecord tablici.
Podaci i ID-evi ostaju isti; ovo nije specifikacija za rebuild postojece baze.

### AppUser

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| version | version | version | long | NE | @Version |
| name | name | name | String | NE | length=100 |
| email | email | email | String | NE | UNIQUE; length=254 |
| passwordHash | password_hash | passwordHash | String | NE | length=255 |
| activeVehicle | active_vehicle_id | activeVehicle_id | Vehicle | DA | FK; LAZY |

### VehicleVariant

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| code | code | code | String | NE | UNIQUE; length=80 |
| make | make | make | String | NE | length=100 |
| model | model | model | String | NE | length=150 |
| generation | generation | generation | String | NE | length=200 |
| engineLabel | engine_label | engineLabel | String | NE | length=240 |
| bodyType | body_type | bodyType | String | DA | length=100 |
| fuelType | fuel_type | fuelType | String | DA | length=80 |
| powerHp | power_hp | powerHp | Integer | DA |  |
| transmission | transmission | transmission | String | DA | length=120 |
| yearFrom | year_from | yearFrom | int | NE |  |
| yearTo | year_to | yearTo | Integer | DA |  |
| imagePath | image_path | imagePath | String | DA | length=255 |

### Vehicle

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| version | version | version | long | NE | @Version |
| owner | owner_id | owner_id | AppUser | NE | FK; LAZY |
| variant | variant_id | variant_id | VehicleVariant | NE | FK; LAZY |
| productionYear | production_year | productionYear | int | NE |  |
| currentMileage | current_mileage | currentMileage | int | NE |  |

### WorkDefinition

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| code | code | code | String | NE | UNIQUE; length=80 |
| name | name | name | String | NE | length=160 |
| category | category | category | WorkCategory | NE | length=20; EnumType.STRING |
| defaultIntervalKm | — | defaultIntervalKm | Integer | DA | MAINTENANCE fallback; pozitivno kada postoji |
| defaultIntervalMonths | — | defaultIntervalMonths | Integer | DA | MAINTENANCE fallback; pozitivno kada postoji |
| defaultEstimatedPrice | default_estimated_price | defaultEstimatedPrice | BigDecimal | DA | decimal(9,2) |
| estimateNote | estimate_note | estimateNote | String | DA | length=1000 |

### VehicleWorkRule

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| variant | variant_id | variant_id | VehicleVariant | NE | FK; LAZY |
| work | work_id | work_id | WorkDefinition | NE | FK; LAZY |
| intervalKm | interval_km | intervalKm | Integer | DA |  |
| scheduleKind | schedule_kind | scheduleKind | ScheduleKind | NE | length=24; EnumType.STRING |
| intervalMonths | interval_months | intervalMonths | Integer | DA |  |
| estimatedPrice | estimated_price | estimatedPrice | BigDecimal | DA | decimal(9,2) |
| intervalSource | interval_source | intervalSource | String | DA | length=1000 |
| estimateNote | estimate_note | estimateNote | String | DA | length=1000 |

### ServiceRecord

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| vehicle | vehicle_id | vehicle_id | Vehicle | NE | FK; LAZY |
| serviceDate | service_date | serviceDate | LocalDate | NE |  |
| mileage | mileage | mileage | int | NE |  |
| note | note | note | String | DA | length=2000 |

V2 vise ne mapira `ServiceRecord.requestKey`; stari `request_key` se u name-only migraciji ne
preimenuje niti automatski brise jer je to potencijalni gubitak povijesnih podataka. Na izoliranoj
kopiji moze se zasebno procijeniti njegovo uklanjanje nakon pregleda ovisnosti. `Problem.requestKey`
ostaje jer je dijagnosticki tok zamrznut.

### ServiceItem

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| serviceRecord | service_record_id | serviceRecord_id | ServiceRecord | NE | FK; LAZY |
| work | work_id | work_id | WorkDefinition | NE | FK; LAZY |
| actualPrice | actual_price | actualPrice | BigDecimal | DA | decimal(9,2) |

### Problem

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| version | version | version | long | NE | @Version |
| vehicle | vehicle_id | vehicle_id | Vehicle | NE | FK; LAZY |
| requestKey | request_key | requestKey | String | NE | UNIQUE; length=36 |
| description | description | description | String | NE | length=2000 |
| status | status | status | ProblemStatus | NE | length=20; EnumType.STRING |
| createdAt | created_at | createdAt | LocalDateTime | NE |  |
| suggestedRepair | suggested_repair_id | suggestedRepair_id | WorkDefinition | DA | FK; LAZY |
| matchPercent | match_percent | matchPercent | BigDecimal | DA | decimal(5,2) |
| estimatedCost | estimated_cost | estimatedCost | BigDecimal | DA | decimal(9,2) |
| estimateNote | estimate_note | estimateNote | String | DA | length=1000 |
| resolvedByService | resolved_by_service_id | resolvedByService_id | ServiceRecord | DA | FK; LAZY |

### DiagnosticRule

| Java atribut poslije | SQL prije | SQL poslije | Tip u Javi | NULL | Dodatan ugovor |
|---|---|---|---|---|---|
| id | id | id | Long | NE | PK; IDENTITY |
| code | code | code | String | NE | UNIQUE; length=80 |
| candidate | candidate_id | candidate_id | WorkDefinition | NE | FK; LAZY |
| phrase | phrase | phrase | String | NE | length=160 |
| weight | weight | weight | int | NE |  |
| active | active | active | boolean | NE |  |

## 4. Veze i slozeni ugovori

- AppUser.activeVehicle je opcionalan u SQL-u samo da registracija moze prvo upisati racun pa vozilo
  u istoj transakciji. Normalno dovrsen racun ima vlastito aktivno vozilo. Nemoj ga naivno pretvoriti u
  NOT NULL prije rjesavanja ciklusa INSERT-a ili u OneToOne samo radi naziva.
- ServiceRecord.items ostaje `mappedBy="serviceRecord"`, cascade ALL/orphanRemoval. Nijedan katalog
  nema cascade remove. @OrderBy("id ASC") cuva redoslijed prikaza koji se vec koristi.
- ServiceItem ima UNIQUE(serviceRecord_id,work_id); VehicleWorkRule UNIQUE(variant_id,work_id).
- Problem.resolvedByService ostaje opcionalni FK i stanje OPEN/RESOLVED mora biti uskladeno s njim.
- Indeksi na vehicle owner, variant picker, service history i problem status ostaju i nakon renamea.
- JPA `length`/`nullable` nije zamjena za provjeru korisnickog unosa. Constraints u DB-u su dodatna zastita.

## 5. Posljedice izvan entity datoteke

Za User -> AppUser provjeri konkretni tip, constructor, generic type, class literal, persistence.xml,
JPQL entity name i testne cleanup upite. Ne preimenuj UserRepository bez potrebe.
Za productionYear/serviceDate provjeri property putanje u JPQL-u, kriterije sortiranja/projekcije,
domenske gettere i pozivatelje. Nisu sva getYear/getDate imena u projektu isti simbol.

Native SQL, SQLSeedTool, ImagePathTool, ReviewedIntervalTool i INFORMATION_SCHEMA/sys.* provjere
koriste fizicke nazive. Samo Java refactor ih nece automatski promijeniti. Ulazni CSV headeri ostaju
stabilni; explicit developer mapping prevodi na ciljna SQL imena. Import ne ulazi u runtime JAR.

V2 dodaje samo dva nullable `INT` stupca na ciljnu `dbo.WorkDefinition` tablicu. Skripta
`schema/05_student_simplification_v2.sql` je eksplicitna, idempotentna i zadano read-only; ne radi
reset baze niti masovni backfill. `hibernate.hbm2ddl.auto=validate` ocekuje ta dva stupca nakon
target-name migracije. Postojeci `VehicleWorkRule` retci ostaju glavni izvor specificnih intervala,
a novi defaulti mogu ostati NULL.

Referenca ne preimenuje constraint/index imena, ne brise constraints radi "lijepog" DDL-a i ne dodaje
nova polja za podatke koji su vec izvedeni. `validate` sam ne provjerava potpunu poslovnu konzistentnost.
