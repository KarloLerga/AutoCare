-- AutoCare finalni studentski cleanup. Azure SQL Database / SQL Server.
-- Zadano je samo citanje. Prvo pregledati rezultat na tocnoj aplikacijskoj bazi.
-- Za primjenu postaviti stvarno ime baze i @Apply = 1 u privatnoj kopiji ove skripte.

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET LOCK_TIMEOUT 15000;

DECLARE @Apply bit = 0;
DECLARE @ExpectedDatabase sysname = N'REPLACE_WITH_ACTUAL_DATABASE';

IF @@TRANCOUNT <> 0
    THROW 51060, 'Pokrenite cleanup u sesiji bez otvorene transakcije.', 1;

IF DB_NAME() IN (N'master', N'model', N'tempdb', N'msdb')
    THROW 51061, 'Odabrana je sistemska baza, ne AutoCare baza.', 1;

DECLARE @Targets TABLE (
    schema_name sysname NOT NULL,
    table_name sysname NOT NULL,
    column_name sysname NOT NULL
);

INSERT INTO @Targets(schema_name, table_name, column_name)
VALUES
    (N'dbo', N'app_user', N'version'),
    (N'dbo', N'vehicle', N'version'),
    (N'dbo', N'problem', N'version'),
    (N'dbo', N'problem', N'request_key'),
    (N'dbo', N'service_record', N'request_key'),
    (N'dbo', N'vehicle_work_rule', N'schedule_kind');

-- Ovaj popis je namjerno prvi i read-only kada je @Apply = 0.
SELECT
    target.schema_name,
    target.table_name,
    target.column_name,
    CASE WHEN column_info.object_id IS NULL THEN 0 ELSE 1 END AS column_exists,
    type_info.name AS type_name,
    column_info.max_length,
    column_info.is_nullable
FROM @Targets target
LEFT JOIN sys.tables table_info
    ON table_info.schema_id = SCHEMA_ID(target.schema_name)
   AND table_info.name = target.table_name
LEFT JOIN sys.columns column_info
    ON column_info.object_id = table_info.object_id
   AND column_info.name = target.column_name
LEFT JOIN sys.types type_info
    ON type_info.user_type_id = column_info.user_type_id
ORDER BY target.table_name, target.column_name;

SELECT
    target.table_name,
    target.column_name,
    default_constraint.name AS default_constraint_name,
    default_constraint.definition
FROM @Targets target
JOIN sys.tables table_info
    ON table_info.schema_id = SCHEMA_ID(target.schema_name)
   AND table_info.name = target.table_name
JOIN sys.columns column_info
    ON column_info.object_id = table_info.object_id
   AND column_info.name = target.column_name
JOIN sys.default_constraints default_constraint
    ON default_constraint.parent_object_id = column_info.object_id
   AND default_constraint.parent_column_id = column_info.column_id
ORDER BY target.table_name, target.column_name;

SELECT
    target.table_name,
    target.column_name,
    index_info.name AS index_name,
    index_info.is_unique,
    index_info.is_primary_key,
    index_info.is_unique_constraint,
    index_column.is_included_column
FROM @Targets target
JOIN sys.tables table_info
    ON table_info.schema_id = SCHEMA_ID(target.schema_name)
   AND table_info.name = target.table_name
JOIN sys.columns column_info
    ON column_info.object_id = table_info.object_id
   AND column_info.name = target.column_name
JOIN sys.index_columns index_column
    ON index_column.object_id = column_info.object_id
   AND index_column.column_id = column_info.column_id
JOIN sys.indexes index_info
    ON index_info.object_id = index_column.object_id
   AND index_info.index_id = index_column.index_id
WHERE index_info.name IS NOT NULL
ORDER BY target.table_name, target.column_name, index_info.name;

SELECT
    target.table_name AS target_table,
    target.column_name AS target_column,
    foreign_key.name AS foreign_key_name,
    OBJECT_SCHEMA_NAME(foreign_key.parent_object_id) AS child_schema,
    OBJECT_NAME(foreign_key.parent_object_id) AS child_table,
    OBJECT_SCHEMA_NAME(foreign_key.referenced_object_id) AS referenced_schema,
    OBJECT_NAME(foreign_key.referenced_object_id) AS referenced_table
FROM @Targets target
JOIN sys.tables target_table
    ON target_table.schema_id = SCHEMA_ID(target.schema_name)
   AND target_table.name = target.table_name
JOIN sys.columns target_column
    ON target_column.object_id = target_table.object_id
   AND target_column.name = target.column_name
JOIN sys.foreign_key_columns foreign_key_column
    ON (foreign_key_column.parent_object_id = target_column.object_id
        AND foreign_key_column.parent_column_id = target_column.column_id)
    OR (foreign_key_column.referenced_object_id = target_column.object_id
        AND foreign_key_column.referenced_column_id = target_column.column_id)
JOIN sys.foreign_keys foreign_key
    ON foreign_key.object_id = foreign_key_column.constraint_object_id
ORDER BY target.table_name, target.column_name, foreign_key.name;

SELECT
    target.table_name,
    target.column_name,
    check_constraint.name AS check_constraint_name,
    check_constraint.definition
FROM @Targets target
JOIN sys.tables table_info
    ON table_info.schema_id = SCHEMA_ID(target.schema_name)
   AND table_info.name = target.table_name
JOIN sys.columns column_info
    ON column_info.object_id = table_info.object_id
   AND column_info.name = target.column_name
JOIN sys.check_constraints check_constraint
    ON check_constraint.parent_object_id = column_info.object_id
WHERE check_constraint.parent_column_id = column_info.column_id
   OR EXISTS (
        SELECT 1
        FROM sys.sql_expression_dependencies dependency
        WHERE dependency.referencing_id = check_constraint.object_id
          AND dependency.referenced_id = column_info.object_id
          AND dependency.referenced_minor_id = column_info.column_id
   )
ORDER BY target.table_name, target.column_name, check_constraint.name;

SELECT
    DB_NAME() AS current_database,
    @ExpectedDatabase AS expected_database,
    @Apply AS apply_requested,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS variants_before,
    (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS works_before,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS scoped_rules_before,
    (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule) AS diagnostic_rules_before;

IF @Apply = 0
BEGIN
    PRINT 'READ-ONLY CLEANUP AUDIT ZAVRSEN. Za primjenu koristite tocnu privatnu kopiju i @Apply = 1.';
    RETURN;
END;

IF @ExpectedDatabase = N'REPLACE_WITH_ACTUAL_DATABASE'
    THROW 51062, 'Prije primjene postavite tocno ime aplikacijske baze.', 1;

IF DB_NAME() <> @ExpectedDatabase
    THROW 51063, 'Trenutna baza nije jednaka eksplicitno potvrdenoj bazi.', 1;

IF OBJECT_ID(N'dbo.app_user', N'U') IS NULL
   OR OBJECT_ID(N'dbo.vehicle', N'U') IS NULL
   OR OBJECT_ID(N'dbo.problem', N'U') IS NULL
   OR OBJECT_ID(N'dbo.service_record', N'U') IS NULL
   OR OBJECT_ID(N'dbo.vehicle_work_rule', N'U') IS NULL
    THROW 51064, 'Nedostaje ocekivana AutoCare tablica. Ne stvarajte paralelnu shemu.', 1;

DECLARE @VariantsBefore bigint = (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant);
DECLARE @WorksBefore bigint = (SELECT COUNT_BIG(*) FROM dbo.work_definition);
DECLARE @RulesBefore bigint = (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule);
DECLARE @DiagnosticsBefore bigint = (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule);

IF @VariantsBefore <> 30366
   OR @WorksBefore <> 122
   OR @RulesBefore <> 1650435
   OR @DiagnosticsBefore <> 87
    THROW 51065, 'Pocetni kataloski counts nisu ocekivani; cleanup je zaustavljen.', 1;

IF EXISTS (
    SELECT 1
    FROM dbo.vehicle_work_rule
    GROUP BY variant_id, work_id
    HAVING COUNT_BIG(*) > 1
)
    THROW 51066, 'Postoje duplikati varijanta/rad; cleanup je zaustavljen.', 1;

IF EXISTS (
    SELECT 1
    FROM sys.indexes index_info
    JOIN sys.index_columns index_column
        ON index_column.object_id = index_info.object_id
       AND index_column.index_id = index_info.index_id
    JOIN sys.columns column_info
        ON column_info.object_id = index_column.object_id
       AND column_info.column_id = index_column.column_id
    JOIN @Targets target
        ON target.column_name = column_info.name
       AND target.table_name = OBJECT_NAME(column_info.object_id)
    WHERE index_info.is_primary_key = 1
)
    THROW 51067, 'Legacy kolona je dio primarnog kljuca; potreban je rucni pregled.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @Sql nvarchar(max);
    DECLARE @SchemaName sysname;
    DECLARE @TableName sysname;
    DECLARE @ConstraintName sysname;

    DECLARE default_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT target.schema_name, target.table_name, default_constraint.name
        FROM @Targets target
        JOIN sys.tables table_info
            ON table_info.schema_id = SCHEMA_ID(target.schema_name)
           AND table_info.name = target.table_name
        JOIN sys.columns column_info
            ON column_info.object_id = table_info.object_id
           AND column_info.name = target.column_name
        JOIN sys.default_constraints default_constraint
            ON default_constraint.parent_object_id = column_info.object_id
           AND default_constraint.parent_column_id = column_info.column_id;

    OPEN default_cursor;
    FETCH NEXT FROM default_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @Sql = N'ALTER TABLE ' + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName)
            + N' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName) + N';';
        EXEC sys.sp_executesql @Sql;
        FETCH NEXT FROM default_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE default_cursor;
    DEALLOCATE default_cursor;

    DECLARE foreign_key_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT DISTINCT
            OBJECT_SCHEMA_NAME(foreign_key.parent_object_id),
            OBJECT_NAME(foreign_key.parent_object_id),
            foreign_key.name
        FROM @Targets target
        JOIN sys.tables target_table
            ON target_table.schema_id = SCHEMA_ID(target.schema_name)
           AND target_table.name = target.table_name
        JOIN sys.columns target_column
            ON target_column.object_id = target_table.object_id
           AND target_column.name = target.column_name
        JOIN sys.foreign_key_columns foreign_key_column
            ON (foreign_key_column.parent_object_id = target_column.object_id
                AND foreign_key_column.parent_column_id = target_column.column_id)
            OR (foreign_key_column.referenced_object_id = target_column.object_id
                AND foreign_key_column.referenced_column_id = target_column.column_id)
        JOIN sys.foreign_keys foreign_key
            ON foreign_key.object_id = foreign_key_column.constraint_object_id;

    OPEN foreign_key_cursor;
    FETCH NEXT FROM foreign_key_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @Sql = N'ALTER TABLE ' + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName)
            + N' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName) + N';';
        EXEC sys.sp_executesql @Sql;
        FETCH NEXT FROM foreign_key_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE foreign_key_cursor;
    DEALLOCATE foreign_key_cursor;

    DECLARE check_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT DISTINCT target.schema_name, target.table_name, check_constraint.name
        FROM @Targets target
        JOIN sys.tables table_info
            ON table_info.schema_id = SCHEMA_ID(target.schema_name)
           AND table_info.name = target.table_name
        JOIN sys.columns column_info
            ON column_info.object_id = table_info.object_id
           AND column_info.name = target.column_name
        JOIN sys.check_constraints check_constraint
            ON check_constraint.parent_object_id = column_info.object_id
        WHERE check_constraint.parent_column_id = column_info.column_id
           OR EXISTS (
                SELECT 1
                FROM sys.sql_expression_dependencies dependency
                WHERE dependency.referencing_id = check_constraint.object_id
                  AND dependency.referenced_id = column_info.object_id
                  AND dependency.referenced_minor_id = column_info.column_id
           );

    OPEN check_cursor;
    FETCH NEXT FROM check_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @Sql = N'ALTER TABLE ' + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName)
            + N' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName) + N';';
        EXEC sys.sp_executesql @Sql;
        FETCH NEXT FROM check_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE check_cursor;
    DEALLOCATE check_cursor;

    DECLARE key_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT DISTINCT target.schema_name, target.table_name, key_constraint.name
        FROM @Targets target
        JOIN sys.tables table_info
            ON table_info.schema_id = SCHEMA_ID(target.schema_name)
           AND table_info.name = target.table_name
        JOIN sys.columns column_info
            ON column_info.object_id = table_info.object_id
           AND column_info.name = target.column_name
        JOIN sys.index_columns index_column
            ON index_column.object_id = column_info.object_id
           AND index_column.column_id = column_info.column_id
        JOIN sys.key_constraints key_constraint
            ON key_constraint.parent_object_id = index_column.object_id
           AND key_constraint.unique_index_id = index_column.index_id;

    OPEN key_cursor;
    FETCH NEXT FROM key_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @Sql = N'ALTER TABLE ' + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName)
            + N' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName) + N';';
        EXEC sys.sp_executesql @Sql;
        FETCH NEXT FROM key_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE key_cursor;
    DEALLOCATE key_cursor;

    DECLARE index_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT DISTINCT target.schema_name, target.table_name, index_info.name
        FROM @Targets target
        JOIN sys.tables table_info
            ON table_info.schema_id = SCHEMA_ID(target.schema_name)
           AND table_info.name = target.table_name
        JOIN sys.columns column_info
            ON column_info.object_id = table_info.object_id
           AND column_info.name = target.column_name
        JOIN sys.index_columns index_column
            ON index_column.object_id = column_info.object_id
           AND index_column.column_id = column_info.column_id
        JOIN sys.indexes index_info
            ON index_info.object_id = index_column.object_id
           AND index_info.index_id = index_column.index_id
        LEFT JOIN sys.key_constraints key_constraint
            ON key_constraint.parent_object_id = index_info.object_id
           AND key_constraint.unique_index_id = index_info.index_id
        WHERE index_info.name IS NOT NULL
          AND index_info.is_primary_key = 0
          AND key_constraint.object_id IS NULL;

    OPEN index_cursor;
    FETCH NEXT FROM index_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @Sql = N'DROP INDEX ' + QUOTENAME(@ConstraintName) + N' ON '
            + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName) + N';';
        EXEC sys.sp_executesql @Sql;
        FETCH NEXT FROM index_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE index_cursor;
    DEALLOCATE index_cursor;

    DECLARE column_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT target.schema_name, target.table_name, target.column_name
        FROM @Targets target;

    OPEN column_cursor;
    FETCH NEXT FROM column_cursor INTO @SchemaName, @TableName, @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        IF COL_LENGTH(@SchemaName + N'.' + @TableName, @ConstraintName) IS NOT NULL
        BEGIN
            SET @Sql = N'ALTER TABLE ' + QUOTENAME(@SchemaName) + N'.' + QUOTENAME(@TableName)
                + N' DROP COLUMN ' + QUOTENAME(@ConstraintName) + N';';
            EXEC sys.sp_executesql @Sql;
        END;
        FETCH NEXT FROM column_cursor INTO @SchemaName, @TableName, @ConstraintName;
    END;
    CLOSE column_cursor;
    DEALLOCATE column_cursor;

    IF (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) <> @VariantsBefore
       OR (SELECT COUNT_BIG(*) FROM dbo.work_definition) <> @WorksBefore
       OR (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) <> @RulesBefore
       OR (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule) <> @DiagnosticsBefore
        THROW 51068, 'Counts su se promijenili; transakcija ce biti vracena.', 1;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT
    DB_NAME() AS current_database,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS variants_after,
    (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS works_after,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS scoped_rules_after,
    (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule) AS diagnostic_rules_after;

SELECT target.table_name, target.column_name,
       CASE WHEN COL_LENGTH(target.schema_name + N'.' + target.table_name, target.column_name) IS NULL
            THEN 0 ELSE 1 END AS legacy_column_still_exists
FROM @Targets target
ORDER BY target.table_name, target.column_name;

PRINT 'Finalni studentski cleanup je primijenjen uz ocuvane kataloske counts.';
