# Završni audit i odluke nakon profesorovog zadnjeg reviewa

## Funkcionalni model

Profesorov finalni workflow razdvaja tri odgovornosti:

1. Bilješke - slobodan opis onoga što vlasnik primjećuje.
2. Katalog - informativni raspon troška standardnog zahvata.
3. Servisi - stvarno odrađeni radovi i stvarno plaćene cijene nakon odlaska kod mehaničara.

Zbog toga su uklonjeni automatska dijagnostika i per-variant matrica pravila.

## Baza

Finalni referentni podaci:

- 30.366 `vehicle_variant`
- 120 `work_definition`
- 600 `work_price_range`
- 30 maintenance radova
- 90 repair radova
- 5 vehicle price classes

Finalni model namjerno nema tablice `vehicle_work_rule` i `diagnostic_rule`.

`problem` više nema `suggested_repair_id` ni `estimated_cost`; dobiva `category`.

`work_definition` dobiva `catalog_category`, `interval_km` i `interval_months`.

`vehicle_variant` dobiva `price_class`.

## JPA

- field access;
- minimalne potrebne anotacije;
- camelCase Java + snake_case SQL preko Hibernate naming strategyja;
- `EntityManagerFactory` jednom;
- kratkotrajni `EntityManager` po use-caseu;
- eksplicitna transakcija u Serviceu kada operacija dira više objekata/repositoryja;
- runtime `hbm2ddl.auto=none` jer se završna postojeća Azure baza migrira guarded SQL skriptom.

## Cijene

Cijene su informativni min-max rasponi po širokoj klasi vozila. Rasponi su precomputed seed podaci. Runtime nema AI, fallback matematiku ni vanjske API pozive.

Stvarni trošak dolazi isključivo iz `ServiceItem.actualPrice`.

## Studentski stil

Namjerno se izbjegavaju lambda/Stream/Optional/var/record, generic transaction helperi i dodatni frameworki. Dulji eksplicitni `begin/commit/rollback` kod je prihvatljiv jer je lakši za objasniti na obrani.
