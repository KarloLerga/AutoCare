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
  AND name IN ('interval_source', 'estimate_note');

SELECT COUNT(*) AS legacy_work_default_columns
FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.work_definition')
  AND name IN ('default_interval_km','default_interval_months','default_estimated_price','estimate_note');

SELECT COUNT(*) AS legacy_problem_columns
FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.problem')
  AND name IN ('match_percent','estimate_note');

/* Sanity: no obvious BEV + combustion-only rule.
   Adjust fuel column/table naming only if actual schema differs; do not silently skip the check. */
SELECT TOP (100)
    variant.code AS variant_code,
    variant.make,
    variant.model,
    variant.fuel_type,
    work.code AS work_code
FROM dbo.vehicle_work_rule r
JOIN dbo.vehicle_variant variant ON variant.id = r.variant_id
JOIN dbo.work_definition work ON work.id = r.work_id
WHERE UPPER(COALESCE(variant.fuel_type,'')) IN ('ELECTRIC','BEV')
  AND work.code IN ('OIL_SERVICE','SPARK_PLUGS','TIMING_BELT_PUMP','DPF_REPLACEMENT');
