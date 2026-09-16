# Azure SQL / JPA field and relation contract - AF3

Source of truth: actual entity annotations plus this AF3 decision. Hibernate creates dbo tables in an EXISTING database. A successful real provider validate is still required. SQL names are not MySQL aliases. Long @Version is BIGINT, NOT SQL Server rowversion. Text uses nationalized character data; enum strings may use VARCHAR as selected by the dialect. LocalDateTime is datetime2, with provider-selected precision.

## User -> dbo.app_user

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `version` (long) | `version` BIGINT | NO | `@Version @Column(nullable=false)` |
| `name` (String) | `name` NVARCHAR(100) | NO | `@Column(nullable=false,length=100)` |
| `email` (String) | `email` NVARCHAR(254) | NO | `@Column(nullable=false,unique=true,length=254)` |
| `passwordHash` (String) | `password_hash` NVARCHAR(255) | NO | `@Column(name="password_hash",nullable=false,length=255)` |
| `activeVehicle` (Vehicle) | `active_vehicle_id` BIGINT FK | YES | `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="active_vehicle_id")` |

## VehicleVariant -> dbo.vehicle_variant

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `code` (String) | `code` NVARCHAR(80) | NO | `@Column(nullable=false,unique=true,length=80)` |
| `make` (String) | `make` NVARCHAR(100) | NO | `@Column(nullable=false,length=100)` |
| `model` (String) | `model` NVARCHAR(150) | NO | `@Column(nullable=false,length=150)` |
| `generation` (String) | `generation` NVARCHAR(200) | NO | `@Column(nullable=false,length=200)` |
| `engineLabel` (String) | `engine_label` NVARCHAR(240) | NO | `@Column(name="engine_label",nullable=false,length=240)` |
| `bodyType` (String) | `body_type` NVARCHAR(100) | YES | `@Column(name="body_type",length=100)` |
| `fuelType` (String) | `fuel_type` NVARCHAR(80) | YES | `@Column(name="fuel_type",length=80)` |
| `powerHp` (Integer) | `power_hp` INT | YES | `@Column(name="power_hp")` |
| `transmission` (String) | `transmission` NVARCHAR(120) | YES | `@Column(length=120)` |
| `yearFrom` (int) | `year_from` INT | NO | `@Column(name="year_from",nullable=false)` |
| `yearTo` (Integer) | `year_to` INT | YES | `@Column(name="year_to")` |
| `imagePath` (String) | `image_path` NVARCHAR(255) | YES | `@Column(name="image_path",length=255)` |

## Vehicle -> dbo.vehicle

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `version` (long) | `version` BIGINT | NO | `@Version @Column(nullable=false)` |
| `owner` (User) | `owner_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false)` |
| `variant` (VehicleVariant) | `variant_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="variant_id",nullable=false)` |
| `year` (int) | `production_year` INT | NO | `@Column(name="production_year",nullable=false)` |
| `currentMileage` (int) | `current_mileage` INT | NO | `@Column(name="current_mileage",nullable=false)` |

## WorkDefinition -> dbo.work_definition

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `code` (String) | `code` NVARCHAR(80) | NO | `@Column(nullable=false,unique=true,length=80)` |
| `name` (String) | `name` NVARCHAR(160) | NO | `@Column(nullable=false,length=160)` |
| `category` (WorkCategory) | `category` WorkCategory STRING | NO | `@Enumerated(EnumType.STRING) @Column(nullable=false,length=20)` |
| `defaultEstimatedPrice` (BigDecimal) | `default_estimated_price` DECIMAL(9,2) | YES | `@Column(name="default_estimated_price",precision=9,scale=2)` |
| `estimateNote` (String) | `estimate_note` NVARCHAR(1000) | YES | `@Column(name="estimate_note",length=1000)` |

## VehicleWorkRule -> dbo.vehicle_work_rule

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `variant` (VehicleVariant) | `variant_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="variant_id",nullable=false)` |
| `work` (WorkDefinition) | `work_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="work_id",nullable=false)` |
| `intervalKm` (Integer) | `interval_km` INT | YES | `@Column(name="interval_km")` |
| `scheduleKind` (ScheduleKind) | `schedule_kind` ScheduleKind STRING | NO | `@Enumerated(EnumType.STRING) @Column(name="schedule_kind",nullable=false,length=24)` |
| `intervalMonths` (Integer) | `interval_months` INT | YES | `@Column(name="interval_months")` |
| `estimatedPrice` (BigDecimal) | `estimated_price` DECIMAL(9,2) | YES | `@Column(name="estimated_price",precision=9,scale=2)` |
| `intervalSource` (String) | `interval_source` NVARCHAR(1000) | YES | `@Column(name="interval_source",length=1000)` |
| `estimateNote` (String) | `estimate_note` NVARCHAR(1000) | YES | `@Column(name="estimate_note",length=1000)` |

## ServiceRecord -> dbo.service_record

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `vehicle` (Vehicle) | `vehicle_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="vehicle_id",nullable=false)` |
| `requestKey` (String) | `request_key` NVARCHAR(36) | NO | `@Column(name="request_key",nullable=false,unique=true,length=36)` |
| `date` (LocalDate) | `service_date` DATE | NO | `@Column(name="service_date",nullable=false)` |
| `mileage` (int) | `mileage` INT | NO | `@Column(nullable=false)` |
| `note` (String) | `note` NVARCHAR(2000) | YES | `@Column(length=2000)` |
| items | No column on this side | - | inverse mappedBy=serviceRecord, cascade ALL, orphanRemoval |

## ServiceItem -> dbo.service_item

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `serviceRecord` (ServiceRecord) | `service_record_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="service_record_id",nullable=false)` |
| `work` (WorkDefinition) | `work_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="work_id",nullable=false)` |
| `actualPrice` (BigDecimal) | `actual_price` DECIMAL(9,2) | YES | `@Column(name="actual_price",precision=9,scale=2)` |

## Problem -> dbo.problem

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `version` (long) | `version` BIGINT | NO | `@Version @Column(nullable=false)` |
| `vehicle` (Vehicle) | `vehicle_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="vehicle_id",nullable=false)` |
| `requestKey` (String) | `request_key` NVARCHAR(36) | NO | `@Column(name="request_key",nullable=false,unique=true,length=36)` |
| `description` (String) | `description` NVARCHAR(2000) | NO | `@Column(nullable=false,length=2000)` |
| `status` (ProblemStatus) | `status` ProblemStatus STRING | NO | `@Enumerated(EnumType.STRING) @Column(nullable=false,length=20)` |
| `createdAt` (LocalDateTime) | `created_at` DATETIME2 | NO | `@Column(name="created_at",nullable=false)` |
| `suggestedRepair` (WorkDefinition) | `suggested_repair_id` BIGINT FK | YES | `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="suggested_repair_id")` |
| `matchPercent` (BigDecimal) | `match_percent` DECIMAL(5,2) | YES | `@Column(name="match_percent",precision=5,scale=2)` |
| `estimatedCost` (BigDecimal) | `estimated_cost` DECIMAL(9,2) | YES | `@Column(name="estimated_cost",precision=9,scale=2)` |
| `estimateNote` (String) | `estimate_note` NVARCHAR(1000) | YES | `@Column(name="estimate_note",length=1000)` |
| `resolvedByService` (ServiceRecord) | `resolved_by_service_id` BIGINT FK | YES | `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="resolved_by_service_id")` |

## DiagnosticRule -> dbo.diagnostic_rule

| Java field | SQL column/type | Null | Mapping |
|---|---|---|---|
| `id` (Long) | `id` BIGINT IDENTITY PRIMARY KEY | NO | `@Id @GeneratedValue(strategy=GenerationType.IDENTITY)` |
| `code` (String) | `code` NVARCHAR(80) | NO | `@Column(nullable=false,unique=true,length=80)` |
| `candidate` (WorkDefinition) | `candidate_id` BIGINT FK | NO | `@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="candidate_id",nullable=false)` |
| `phrase` (String) | `phrase` NVARCHAR(160) | NO | `@Column(nullable=false,length=160)` |
| `weight` (int) | `weight` INT | NO | `@Column(nullable=false)` |
| `active` (boolean) | `active` BIT | NO | `@Column(nullable=false)` |

## Relacije

| Owning side / FK | Target | Fetch | Cascade/delete |
|---|---|---|---|
| User.activeVehicle / active_vehicle_id nullable, NOT UNIQUE | Vehicle | LAZY | None. Ownership enforced by service/domain; nullable only during onboarding or explicit deletion choreography. |
| Vehicle.owner / owner_id | User | LAZY | None; owner row locked before authenticated writes. |
| Vehicle.variant / variant_id | VehicleVariant | LAZY | Never cascade catalog deletion. |
| ServiceRecord.vehicle / vehicle_id | Vehicle | LAZY | None. |
| ServiceItem.serviceRecord / service_record_id | ServiceRecord | LAZY | Owning side; parent items inverse mappedBy, only real ALL + orphanRemoval collection. |
| ServiceItem.work / work_id | WorkDefinition | LAZY | None, preserve catalog/history. |
| VehicleWorkRule.variant / variant_id and work / work_id | VehicleVariant, WorkDefinition | LAZY | None; UNIQUE pair. |
| Problem.vehicle / vehicle_id | Vehicle | LAZY | None. |
| Problem.suggestedRepair / suggested_repair_id nullable | WorkDefinition | LAZY | None; candidate must REPAIR. |
| Problem.resolvedByService / resolved_by_service_id nullable | ServiceRecord | LAZY | None; one service can resolve many problems. |
| DiagnosticRule.candidate / candidate_id | WorkDefinition | LAZY | None; REPAIR only in domain/import validation. |

FK delete actions are not a replacement for JPA cascade or service choreography. Delete vehicle: lock owner -> enforce not last -> switch active if needed -> delete problems -> items -> records -> vehicle. No deleting shared catalogs. No ManyToMany. No inverse User.vehicles/Vehicle.history collections just for symmetry.

## Constraints and generation

Unique: canonical email; catalog/work/diagnostic code; service/problem request_key; (variant_id,work_id); (service_record_id,work_id). All declared indexes remain. Hibernate and database enforce FK/unique/null/type boundaries. Cross-row ownership, at-least-one-vehicle, nonempty service, repair classification and schedule semantics are additionally service/domain invariants; do not pretend all are expressible as simple SQL CHECKs.

New database: explicit schema-update, then validate. An existing earlier SQL Server schema may require an explicit reviewed data backfill before making schedule_kind NOT NULL: FIXED when interval_km or interval_months exists, CONDITION_BASED for repairs, otherwise UNKNOWN. Do not run MySQL DDL. Do not drop data to fix migration errors. For this delivery no schema change has actually been executed on Azure.

Physical ERD active FK allows multiple referencing user rows because there is no UNIQUE. The stricter logical ownership relation is enforced by the application; domain cardinality is not misrepresented as a database unique constraint.
