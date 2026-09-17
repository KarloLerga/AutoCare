/* AutoCare final catalogue audit. READ ONLY. */
SET NOCOUNT ON;

SELECT DB_NAME() AS current_database;

SELECT COUNT(*) AS vehicle_variant_count FROM dbo.vehicle_variant;
SELECT COUNT(*) AS work_definition_count FROM dbo.work_definition;
SELECT COUNT(*) AS vehicle_work_rule_count FROM dbo.vehicle_work_rule;

SELECT work.category, COUNT(*) AS rule_count
FROM dbo.vehicle_work_rule r
JOIN dbo.work_definition work ON work.id = r.work_id
GROUP BY work.category
ORDER BY work.category;

SELECT COUNT(*) AS null_or_nonpositive_prices
FROM dbo.vehicle_work_rule
WHERE estimated_price IS NULL OR estimated_price <= 0;

SELECT COUNT(*) AS maintenance_without_interval
FROM dbo.vehicle_work_rule r
JOIN dbo.work_definition work ON work.id = r.work_id
WHERE work.category = 'MAINTENANCE'
  AND r.interval_km IS NULL
  AND r.interval_months IS NULL;

SELECT COUNT(*) AS repair_with_interval
FROM dbo.vehicle_work_rule r
JOIN dbo.work_definition work ON work.id = r.work_id
WHERE work.category = 'REPAIR'
  AND (r.interval_km IS NOT NULL OR r.interval_months IS NOT NULL);

SELECT COUNT(*) AS duplicate_variant_work_groups
FROM (
    SELECT variant_id, work_id
    FROM dbo.vehicle_work_rule
    GROUP BY variant_id, work_id
    HAVING COUNT(*) > 1
) duplicates;

SELECT COUNT(*) AS removed_other_work_count
FROM dbo.work_definition
WHERE code IN ('OTHER_MAINTENANCE', 'OTHER_REPAIR');

SELECT COUNT(*) AS diagnostic_rule_table_exists
FROM sys.tables
WHERE schema_id = SCHEMA_ID('dbo') AND name = 'diagnostic_rule';

SELECT COUNT(*) AS legacy_rule_columns
FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.vehicle_work_rule')
  AND name IN ('schedule_kind','interval_source','estimate_note');

SELECT COUNT(*) AS legacy_work_default_columns
FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.work_definition')
  AND name IN ('default_interval_km','default_interval_months','default_estimated_price','estimate_note');

SELECT COUNT(*) AS legacy_problem_columns
FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.problem')
  AND name IN ('match_percent','estimate_note');

SELECT COUNT(*) AS work_names_without_expected_croatian_characters
FROM dbo.work_definition
WHERE (code='BRAKE_FLUID' AND name<>N'Izmjena kočione tekućine')
   OR (code='BRAKE_INSPECTION' AND name<>N'Pregled kočnica')
   OR (code='TYRE_MOUNT_BALANCE' AND name<>N'Montaža i balansiranje četiri gume')
   OR (code='TRANSMISSION_REBUILD' AND name<>N'Popravak ili zamjena mjenjača');

SELECT COUNT(*) AS bev_combustion_or_conventional_transmission_rules
FROM dbo.vehicle_work_rule r
JOIN dbo.vehicle_variant variant ON variant.id=r.variant_id
JOIN dbo.work_definition work ON work.id=r.work_id
WHERE UPPER(COALESCE(variant.fuel_type,'')) IN ('ELECTRIC','BEV')
  AND work.code IN (
    'OIL_SERVICE','AIR_FILTER','FUEL_FILTER','TIMING_BELT_PUMP','SPARK_PLUGS',
    'AUX_BELT','MANUAL_GEARBOX_OIL','AUTO_GEARBOX_OIL','GLOW_PLUGS','TURBO',
    'EGR_VALVE','DPF_CLEAN','DPF_REPLACEMENT','DIESEL_INJECTOR','IGNITION_COIL',
    'CLUTCH_KIT','CLUTCH_DMF','ENGINE_OIL_LEAK_TEST','HEAD_GASKET','TIMING_CHAIN',
    'TRANSMISSION_REBUILD');

SELECT COUNT(*) AS explicit_fwd_differential_or_transfer_rules
FROM dbo.vehicle_work_rule r
JOIN dbo.vehicle_variant variant ON variant.id=r.variant_id
JOIN dbo.work_definition work ON work.id=r.work_id
WHERE work.code IN ('DIFFERENTIAL_OIL','TRANSFER_CASE_OIL')
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) LIKE '%FWD%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%AWD%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%4WD%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%4X4%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%XDRIVE%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%QUATTRO%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%4MATIC%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%4MOTION%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%ALL4%'
  AND UPPER(COALESCE(variant.model,'') + ' ' + COALESCE(variant.generation,'') + ' ' + COALESCE(variant.engine_label,'')) NOT LIKE '%SH-AWD%';

SELECT
  CASE WHEN (SELECT COUNT(*) FROM dbo.vehicle_variant)=30366 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT(*) FROM dbo.work_definition)=120 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT(*) FROM dbo.vehicle_work_rule)=2908857 THEN 0 ELSE 1 END
  AS unexpected_final_catalogue_counts;
