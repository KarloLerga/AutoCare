# Opseg finalnih dijagrama

## Arhitektura

`architecture.mmd/png` prikazuje tok View -> Controller -> Service -> Repository -> JPA/Hibernate -> Azure SQL. Service je vlasnik transakcije.

## Persistentna domena

`domain.mmd/png` treba prikazivati samo osam persistentnih entiteta: AppUser, VehicleVariant, Vehicle, WorkDefinition, WorkPriceRange, ServiceRecord, ServiceItem i Problem, plus bitne enumove/kardinalnosti.

Ne prikazivati stare DiagnosticRule ili VehicleWorkRule klase.

## Aplikacijski odnosi

`design.mmd/png` prikazuje reprezentativne Controller/Service/Repository odnose, Maintenance Strategy i AppEvents/listener mehanizam. Nema dijagnostičkog Strategyja.

## ERD

`erd.mmd/png` prikazuje finalne fizičke `dbo` tablice i važne PK/FK veze. Finalni ERD uključuje `work_price_range`, `vehicle_variant.price_class`, `work_definition.catalog_category/interval_*` i `problem.category`.

Ne prikazuje `vehicle_work_rule`, `diagnostic_rule`, `suggested_repair_id` ni `estimated_cost` jer nakon migracije ne postoje.
