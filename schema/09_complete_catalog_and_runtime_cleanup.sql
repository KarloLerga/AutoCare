-- Guarded final migration. Default is a read-only audit; the setup tool changes @Apply to 1
-- only after the caller explicitly supplies --apply --confirm-final-schema.
SET NOCOUNT ON;
DECLARE @Apply bit = 0;

IF DB_NAME() IN (N'master', N'model', N'msdb', N'tempdb')
  THROW 51000, 'Sistemska baza nije dopuštena za AutoCare migraciju.', 1;

SELECT
  DB_NAME() AS database_name,
  (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS variants_before,
  (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS works_before,
  (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS rules_before,
  (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE code IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR')) AS other_works_before,
  CASE WHEN OBJECT_ID(N'dbo.diagnostic_rule', N'U') IS NULL THEN 0 ELSE 1 END AS legacy_rule_table_before;

IF @Apply = 0
  RETURN;

BEGIN TRY
  BEGIN TRANSACTION;

  IF (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) <> 30366
    THROW 51001, 'Očekuje se 30366 varijanti prije završne migracije.', 1;
  IF (SELECT COUNT_BIG(*) FROM dbo.work_definition
      WHERE code NOT IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR')) <> 120
    THROW 51002, 'Očekuje se 120 finalnih definicija rada.', 1;
  IF (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) <> 2908857
    THROW 51003, 'Prvo treba uvesti kompletni materializirani katalog.', 1;

  IF EXISTS (
      SELECT 1 FROM dbo.service_item i JOIN dbo.work_definition w ON w.id=i.work_id
      WHERE w.code IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR'))
    THROW 51004, 'OTHER rad ima referencu u servisnoj povijesti.', 1;
  IF EXISTS (
      SELECT 1 FROM dbo.problem p JOIN dbo.work_definition w ON w.id=p.suggested_repair_id
      WHERE w.code IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR'))
    THROW 51005, 'OTHER rad ima referencu u problemima.', 1;
  IF EXISTS (
      SELECT 1 FROM dbo.vehicle_work_rule
      WHERE estimated_price IS NULL OR estimated_price <= 0)
    THROW 51006, 'Katalog ima praznu ili nepozitivnu cijenu.', 1;
  IF EXISTS (
      SELECT 1 FROM dbo.vehicle_work_rule r JOIN dbo.work_definition w ON w.id=r.work_id
      WHERE (w.category=N'MAINTENANCE' AND r.interval_km IS NULL AND r.interval_months IS NULL)
         OR (w.category=N'REPAIR' AND (r.interval_km IS NOT NULL OR r.interval_months IS NOT NULL)))
    THROW 51007, 'Katalog ima nevaljan interval.', 1;
  IF EXISTS (
      SELECT 1 FROM sys.foreign_keys
      WHERE referenced_object_id=OBJECT_ID(N'dbo.vehicle_work_rule'))
    THROW 51008, 'Druga tablica referencira vehicle_work_rule.', 1;

  DELETE FROM dbo.vehicle_work_rule
  WHERE work_id IN (SELECT id FROM dbo.work_definition
                    WHERE code IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR'));
  DELETE FROM dbo.work_definition
  WHERE code IN (N'OTHER_MAINTENANCE', N'OTHER_REPAIR');

  DECLARE @TableName sysname, @ColumnName sysname, @ObjectId int, @ColumnId int, @Sql nvarchar(max);
  DECLARE old_columns CURSOR LOCAL FAST_FORWARD FOR
    SELECT table_name, column_name
    FROM (VALUES
      (N'dbo.work_definition', N'default_interval_km'),
      (N'dbo.work_definition', N'default_interval_months'),
      (N'dbo.work_definition', N'default_estimated_price'),
      (N'dbo.work_definition', N'estimate_note'),
      (N'dbo.vehicle_work_rule', N'interval_source'),
      (N'dbo.vehicle_work_rule', N'estimate_note'),
      (N'dbo.problem', N'match_percent'),
      (N'dbo.problem', N'estimate_note')) AS columns_to_remove(table_name, column_name);

  OPEN old_columns;
  FETCH NEXT FROM old_columns INTO @TableName, @ColumnName;
  WHILE @@FETCH_STATUS = 0
  BEGIN
    IF COL_LENGTH(@TableName, @ColumnName) IS NOT NULL
    BEGIN
      SET @ObjectId = OBJECT_ID(@TableName);
      SET @ColumnId = COLUMNPROPERTY(@ObjectId, @ColumnName, 'ColumnId');
      SET @Sql = N'';
      SELECT @Sql = @Sql + N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.'
          + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';'
        FROM sys.default_constraints
        WHERE parent_object_id=@ObjectId AND parent_column_id=@ColumnId;
      SELECT @Sql = @Sql + N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.'
          + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';'
        FROM sys.check_constraints
        WHERE parent_object_id=@ObjectId AND parent_column_id=@ColumnId;
      IF @Sql <> N'' EXEC sys.sp_executesql @Sql;
      SET @Sql = N'ALTER TABLE ' + @TableName + N' DROP COLUMN ' + QUOTENAME(@ColumnName) + N';';
      EXEC sys.sp_executesql @Sql;
    END;
    FETCH NEXT FROM old_columns INTO @TableName, @ColumnName;
  END;
  CLOSE old_columns;
  DEALLOCATE old_columns;

  IF OBJECT_ID(N'dbo.diagnostic_rule', N'U') IS NOT NULL
  BEGIN
    SET @ObjectId = OBJECT_ID(N'dbo.diagnostic_rule');
    SET @Sql = N'';
    SELECT @Sql = @Sql + N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.'
        + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';'
      FROM sys.foreign_keys
      WHERE parent_object_id=@ObjectId OR referenced_object_id=@ObjectId;
    IF @Sql <> N'' EXEC sys.sp_executesql @Sql;
    DROP TABLE dbo.diagnostic_rule;
  END;

  COMMIT TRANSACTION;
END TRY
BEGIN CATCH
  IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
  THROW;
END CATCH;

SELECT
  (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS variants_after,
  (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS works_after,
  (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS rules_after,
  CASE WHEN OBJECT_ID(N'dbo.diagnostic_rule', N'U') IS NULL THEN 0 ELSE 1 END AS legacy_rule_table_after;
