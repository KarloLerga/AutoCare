-- Read-only diagnostics for the final AutoCare snake_case runtime contract. Run in the intended user database, never master.
SET NOCOUNT ON;
SELECT DB_NAME() AS database_name, CAST(SERVERPROPERTY('EngineEdition') AS int) AS engine_edition;
SELECT name AS table_name FROM sys.tables WHERE schema_id=SCHEMA_ID(N'dbo') ORDER BY name;
SELECT t.name AS table_name,c.name AS column_name,ty.name AS type_name,c.max_length,c.precision,c.scale,c.is_nullable,c.is_identity
FROM sys.tables t JOIN sys.columns c ON c.object_id=t.object_id JOIN sys.types ty ON ty.user_type_id=c.user_type_id
WHERE t.schema_id=SCHEMA_ID(N'dbo') ORDER BY t.name,c.column_id;
SELECT OBJECT_NAME(parent_object_id) AS table_name,name AS fk_name,delete_referential_action_desc
FROM sys.foreign_keys WHERE schema_id=SCHEMA_ID(N'dbo');
SELECT OBJECT_NAME(object_id) AS table_name,name,is_unique,is_primary_key FROM sys.indexes
WHERE object_id IN(SELECT object_id FROM sys.tables WHERE schema_id=SCHEMA_ID(N'dbo')) AND name IS NOT NULL;

SELECT COUNT_BIG(*) AS variants FROM dbo.vehicle_variant;
SELECT COUNT_BIG(*) AS works FROM dbo.work_definition;
SELECT COUNT_BIG(*) AS materialized_rules FROM dbo.vehicle_work_rule;
SELECT COUNT_BIG(*) AS null_or_nonpositive_estimates FROM dbo.vehicle_work_rule WHERE estimated_price IS NULL OR estimated_price<=0;
SELECT COUNT_BIG(*) AS maintenance_without_interval
FROM dbo.vehicle_work_rule r JOIN dbo.work_definition w ON w.id=r.work_id
WHERE w.category=N'MAINTENANCE' AND r.interval_km IS NULL AND r.interval_months IS NULL;
SELECT COUNT_BIG(*) AS repair_with_interval
FROM dbo.vehicle_work_rule r JOIN dbo.work_definition w ON w.id=r.work_id
WHERE w.category=N'REPAIR' AND (r.interval_km IS NOT NULL OR r.interval_months IS NOT NULL);
SELECT variant_id,work_id,COUNT_BIG(*) AS duplicates FROM dbo.vehicle_work_rule GROUP BY variant_id,work_id HAVING COUNT_BIG(*)>1;
SELECT code,COUNT_BIG(*) AS duplicates FROM dbo.vehicle_variant GROUP BY code HAVING COUNT_BIG(*)>1;
SELECT code,COUNT_BIG(*) AS duplicates FROM dbo.work_definition GROUP BY code HAVING COUNT_BIG(*)>1;

SELECT COUNT(*) AS diagnostic_rule_table_exists
FROM sys.tables WHERE schema_id=SCHEMA_ID(N'dbo') AND name=N'diagnostic_rule';
SELECT COUNT(*) AS removed_other_work_count
FROM dbo.work_definition WHERE code IN (N'OTHER_MAINTENANCE',N'OTHER_REPAIR');

SELECT u.id AS inconsistent_user,u.active_vehicle_id FROM dbo.app_user u LEFT JOIN dbo.vehicle v ON v.id=u.active_vehicle_id
WHERE u.active_vehicle_id IS NOT NULL AND (v.id IS NULL OR v.owner_id<>u.id);

SELECT TOP(20) v.make,v.model,v.generation,w.code,w.name,r.estimated_price,r.interval_km,r.interval_months
FROM dbo.vehicle_work_rule r JOIN dbo.vehicle_variant v ON v.id=r.variant_id JOIN dbo.work_definition w ON w.id=r.work_id
WHERE v.make=N'Tesla' ORDER BY v.id,w.code;

-- Obvious hardware sanity checks used by the final materialized catalogue.
SELECT COUNT_BIG(*) AS bev_combustion_or_conventional_transmission_rules
FROM dbo.vehicle_work_rule r
JOIN dbo.vehicle_variant v ON v.id=r.variant_id
JOIN dbo.work_definition w ON w.id=r.work_id
WHERE UPPER(COALESCE(v.fuel_type,N'')) IN (N'ELECTRIC',N'BEV')
  AND w.code IN (
    N'OIL_SERVICE',N'AIR_FILTER',N'FUEL_FILTER',N'TIMING_BELT_PUMP',N'SPARK_PLUGS',
    N'AUX_BELT',N'MANUAL_GEARBOX_OIL',N'AUTO_GEARBOX_OIL',N'GLOW_PLUGS',N'TURBO',
    N'EGR_VALVE',N'DPF_CLEAN',N'DPF_REPLACEMENT',N'DIESEL_INJECTOR',N'IGNITION_COIL',
    N'CLUTCH_KIT',N'CLUTCH_DMF',N'ENGINE_OIL_LEAK_TEST',N'HEAD_GASKET',N'TIMING_CHAIN',
    N'TRANSMISSION_REBUILD');

SELECT COUNT_BIG(*) AS explicit_fwd_differential_or_transfer_rules
FROM dbo.vehicle_work_rule r
JOIN dbo.vehicle_variant v ON v.id=r.variant_id
JOIN dbo.work_definition w ON w.id=r.work_id
WHERE w.code IN (N'DIFFERENTIAL_OIL',N'TRANSFER_CASE_OIL')
  AND UPPER(COALESCE(v.model,N'') + N' ' + COALESCE(v.generation,N'') + N' ' + COALESCE(v.engine_label,N'')) LIKE N'%FWD%';

DECLARE @LegacyColumns TABLE (table_name sysname NOT NULL, column_name sysname NOT NULL);
INSERT INTO @LegacyColumns(table_name,column_name)
VALUES
    (N'app_user',N'version'),
    (N'vehicle',N'version'),
    (N'problem',N'version'),
    (N'problem',N'request_key'),
    (N'problem',N'match_percent'),
    (N'problem',N'estimate_note'),
    (N'service_record',N'request_key'),
    (N'vehicle_work_rule',N'schedule_kind'),
    (N'vehicle_work_rule',N'interval_source'),
    (N'vehicle_work_rule',N'estimate_note'),
    (N'work_definition',N'default_interval_km'),
    (N'work_definition',N'default_interval_months'),
    (N'work_definition',N'default_estimated_price'),
    (N'work_definition',N'estimate_note');
SELECT table_name,column_name,
       CASE WHEN COL_LENGTH(N'dbo.' + table_name,column_name) IS NULL THEN 0 ELSE 1 END AS legacy_column_exists
FROM @LegacyColumns
ORDER BY table_name,column_name;
