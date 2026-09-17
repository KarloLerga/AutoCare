# AutoCare — baza i JPA ugovor

## Aktualna odluka

Java koristi čitljiva camelCase/PascalCase imena, a postojeća Azure SQL baza ostaje u fizičkom
`snake_case` obliku. `src/main/resources/META-INF/persistence.xml` koristi
`CamelCaseToUnderscoresNamingStrategy`, pa runtime ne treba desetke `@Column(name=...)` anotacija.
Runtime ima `hibernate.hbm2ddl.auto=none`; DDL i cleanup provjeravaju se zasebnim SQL/setup alatima.

Baza je vlasnik fizičkih duljina, nullabilityja, indeksa, unique/check/FK constrainta, precisiona i
identity postavki. Java entiteti zadržavaju samo ORM značenje: `@Entity`, ID/identity, veze,
`@OneToMany` i string-enum mapiranje.

## Tablice

| Java entitet | SQL tablica |
|---|---|
| `AppUser` | `dbo.app_user` |
| `VehicleVariant` | `dbo.vehicle_variant` |
| `Vehicle` | `dbo.vehicle` |
| `WorkDefinition` | `dbo.work_definition` |
| `VehicleWorkRule` | `dbo.vehicle_work_rule` |
| `ServiceRecord` | `dbo.service_record` |
| `ServiceItem` | `dbo.service_item` |
| `Problem` | `dbo.problem` |
| `DiagnosticRule` | `dbo.diagnostic_rule` |

## Mapirana polja

| Entitet | Java polje → SQL stupac |
|---|---|
| AppUser | `id`, `name`, `email`, `passwordHash → password_hash`, `activeVehicle → active_vehicle_id` |
| VehicleVariant | `id`, `code`, `make`, `model`, `generation`, `engineLabel → engine_label`, `bodyType → body_type`, `fuelType → fuel_type`, `powerHp → power_hp`, `transmission`, `yearFrom → year_from`, `yearTo → year_to`, `imagePath → image_path` |
| Vehicle | `id`, `owner → owner_id`, `variant → variant_id`, `productionYear → production_year`, `currentMileage → current_mileage` |
| WorkDefinition | `id`, `code`, `name`, `category`, `defaultIntervalKm → default_interval_km`, `defaultIntervalMonths → default_interval_months`, `defaultEstimatedPrice → default_estimated_price`, `estimateNote → estimate_note` |
| VehicleWorkRule | `id`, `variant → variant_id`, `work → work_id`, `intervalKm → interval_km`, `intervalMonths → interval_months`, `estimatedPrice → estimated_price`, `intervalSource → interval_source`, `estimateNote → estimate_note` |
| ServiceRecord | `id`, `vehicle → vehicle_id`, `serviceDate → service_date`, `mileage`, `note` |
| ServiceItem | `id`, `serviceRecord → service_record_id`, `work → work_id`, `actualPrice → actual_price` |
| Problem | `id`, `vehicle → vehicle_id`, `description`, `status`, `createdAt → created_at`, `suggestedRepair → suggested_repair_id`, `matchPercent → match_percent`, `estimatedCost → estimated_cost`, `estimateNote → estimate_note`, `resolvedByService → resolved_by_service_id` |
| DiagnosticRule | `id`, `code`, `candidate → candidate_id`, `phrase`, `weight`, `active` |

`ServiceRecord.items` je inverse Java kolekcija (`mappedBy=serviceRecord`) i nema vlastiti stupac.
`ServiceItem.actualPrice` znači stvarno plaćeni iznos; `VehicleWorkRule.estimatedPrice` i
`WorkDefinition.defaultEstimatedPrice` su samo informativne procjene. NULL nije nula.

## Fizički cleanup

`schema/07_final_student_cleanup.sql` je read-only po defaultu. Nakon točnog targeta i pregleda
ovisnosti može ukloniti samo:

- `app_user.version`
- `vehicle.version`
- `problem.version`
- `problem.request_key`
- `service_record.request_key`
- `vehicle_work_rule.schedule_kind`

Skripta preko `sys.*` pronalazi samo pripadajuće default/index/key/FK/check ovisnosti, provjerava
početne i završne counts i ne radi drop tablica, reset, rename, backfill ni re-seed.

## Provjera

Za read-only pregled koristiti `scripts/verify-database.sql`. Završni live audit potvrđuje
`30.366 / 122 / 1.650.435 / 87` za varijante, radove, scoped rules i dijagnostička pravila; duplicate
provjere su 0, a cleanup kolone su odsutne. Stvarni datumi i blokade nalaze se u
`docs/VERIFICATION.md`.
