-- Read-only diagnostics for the current snake_case runtime contract. Run in the intended user database, never master.
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
SELECT COUNT_BIG(*) AS scoped_rules FROM dbo.vehicle_work_rule;
SELECT COUNT_BIG(*) AS diagnostic_rules FROM dbo.diagnostic_rule;
SELECT schedule_kind,COUNT_BIG(*) AS rules FROM dbo.vehicle_work_rule GROUP BY schedule_kind;
SELECT COUNT_BIG(*) AS null_estimates FROM dbo.vehicle_work_rule WHERE estimated_price IS NULL;
SELECT variant_id,work_id,COUNT_BIG(*) AS duplicates FROM dbo.vehicle_work_rule GROUP BY variant_id,work_id HAVING COUNT_BIG(*)>1;
SELECT code,COUNT_BIG(*) AS duplicates FROM dbo.vehicle_variant GROUP BY code HAVING COUNT_BIG(*)>1;
SELECT u.id AS inconsistent_user,u.active_vehicle_id FROM dbo.app_user u LEFT JOIN dbo.vehicle v ON v.id=u.active_vehicle_id
WHERE u.active_vehicle_id IS NULL OR v.owner_id<>u.id;
SELECT TOP(20) v.make,v.model,v.generation,w.code,w.name,r.estimated_price,r.schedule_kind,r.interval_km,r.interval_months,r.interval_source
FROM dbo.vehicle_work_rule r JOIN dbo.vehicle_variant v ON v.id=r.variant_id JOIN dbo.work_definition w ON w.id=r.work_id
WHERE v.make=N'Tesla' ORDER BY v.id,w.code;
-- NULL is unknown/quote, not0. Compare manifest code subsets when legacy/manual records exist.
-- Close/disconnect Object Explorer after use to avoid unnecessarily keeping serverless DB active.
