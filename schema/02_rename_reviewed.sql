-- AutoCare cleanup naming migration. Azure SQL Database / SQL Server.
-- REVIEW TEMPLATE: not executed against a real SQL Server during preparation.
-- Default is read-only. Do not run this batch with a separate transaction open.
-- First test on an isolated copy; verify recovery and stop the app/seed workers.
-- This changes names only. No application data is inserted, updated or deleted.
-- Native SQL/importer queries and SQL modules need a separate reviewed update.
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET LOCK_TIMEOUT 15000;

DECLARE @Apply bit = 0;
DECLARE @TargetDatabase sysname = N'REPLACE_WITH_ACTUAL_DATABASE';
DECLARE @RecoveryVerified bit = 0;
DECLARE @ApplicationStopped bit = 0;
DECLARE @DependenciesReviewed bit = 0;

IF @@TRANCOUNT <> 0
    THROW 51000, 'Use a session without an existing transaction.', 1;
IF DB_NAME() IN (N'master', N'model', N'tempdb', N'msdb')
    THROW 51001, 'Select the application database, not a system database.', 1;

DECLARE @Tables TABLE (BeforeName sysname PRIMARY KEY, AfterName sysname UNIQUE);
INSERT INTO @Tables VALUES
    (N'app_user', N'AppUser'),
    (N'vehicle_variant', N'VehicleVariant'),
    (N'vehicle', N'Vehicle'),
    (N'work_definition', N'WorkDefinition'),
    (N'vehicle_work_rule', N'VehicleWorkRule'),
    (N'service_record', N'ServiceRecord'),
    (N'service_item', N'ServiceItem'),
    (N'problem', N'Problem'),
    (N'diagnostic_rule', N'DiagnosticRule');

DECLARE @Columns TABLE (
    BeforeTable sysname, AfterTable sysname, BeforeName sysname, AfterName sysname,
    PRIMARY KEY (BeforeTable, BeforeName)
);
INSERT INTO @Columns VALUES
    (N'app_user', N'AppUser', N'id', N'id'),
    (N'app_user', N'AppUser', N'version', N'version'),
    (N'app_user', N'AppUser', N'name', N'name'),
    (N'app_user', N'AppUser', N'email', N'email'),
    (N'app_user', N'AppUser', N'password_hash', N'passwordHash'),
    (N'app_user', N'AppUser', N'active_vehicle_id', N'activeVehicle_id'),
    (N'vehicle_variant', N'VehicleVariant', N'id', N'id'),
    (N'vehicle_variant', N'VehicleVariant', N'code', N'code'),
    (N'vehicle_variant', N'VehicleVariant', N'make', N'make'),
    (N'vehicle_variant', N'VehicleVariant', N'model', N'model'),
    (N'vehicle_variant', N'VehicleVariant', N'generation', N'generation'),
    (N'vehicle_variant', N'VehicleVariant', N'engine_label', N'engineLabel'),
    (N'vehicle_variant', N'VehicleVariant', N'body_type', N'bodyType'),
    (N'vehicle_variant', N'VehicleVariant', N'fuel_type', N'fuelType'),
    (N'vehicle_variant', N'VehicleVariant', N'power_hp', N'powerHp'),
    (N'vehicle_variant', N'VehicleVariant', N'transmission', N'transmission'),
    (N'vehicle_variant', N'VehicleVariant', N'year_from', N'yearFrom'),
    (N'vehicle_variant', N'VehicleVariant', N'year_to', N'yearTo'),
    (N'vehicle_variant', N'VehicleVariant', N'image_path', N'imagePath'),
    (N'vehicle', N'Vehicle', N'id', N'id'),
    (N'vehicle', N'Vehicle', N'version', N'version'),
    (N'vehicle', N'Vehicle', N'owner_id', N'owner_id'),
    (N'vehicle', N'Vehicle', N'variant_id', N'variant_id'),
    (N'vehicle', N'Vehicle', N'production_year', N'productionYear'),
    (N'vehicle', N'Vehicle', N'current_mileage', N'currentMileage'),
    (N'work_definition', N'WorkDefinition', N'id', N'id'),
    (N'work_definition', N'WorkDefinition', N'code', N'code'),
    (N'work_definition', N'WorkDefinition', N'name', N'name'),
    (N'work_definition', N'WorkDefinition', N'category', N'category'),
    (N'work_definition', N'WorkDefinition', N'default_estimated_price', N'defaultEstimatedPrice'),
    (N'work_definition', N'WorkDefinition', N'estimate_note', N'estimateNote'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'id', N'id'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'variant_id', N'variant_id'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'work_id', N'work_id'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'interval_km', N'intervalKm'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'schedule_kind', N'scheduleKind'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'interval_months', N'intervalMonths'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'estimated_price', N'estimatedPrice'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'interval_source', N'intervalSource'),
    (N'vehicle_work_rule', N'VehicleWorkRule', N'estimate_note', N'estimateNote'),
    (N'service_record', N'ServiceRecord', N'id', N'id'),
    (N'service_record', N'ServiceRecord', N'vehicle_id', N'vehicle_id'),
    (N'service_record', N'ServiceRecord', N'service_date', N'serviceDate'),
    (N'service_record', N'ServiceRecord', N'mileage', N'mileage'),
    (N'service_record', N'ServiceRecord', N'note', N'note'),
    (N'service_item', N'ServiceItem', N'id', N'id'),
    (N'service_item', N'ServiceItem', N'service_record_id', N'serviceRecord_id'),
    (N'service_item', N'ServiceItem', N'work_id', N'work_id'),
    (N'service_item', N'ServiceItem', N'actual_price', N'actualPrice'),
    (N'problem', N'Problem', N'id', N'id'),
    (N'problem', N'Problem', N'version', N'version'),
    (N'problem', N'Problem', N'vehicle_id', N'vehicle_id'),
    (N'problem', N'Problem', N'request_key', N'requestKey'),
    (N'problem', N'Problem', N'description', N'description'),
    (N'problem', N'Problem', N'status', N'status'),
    (N'problem', N'Problem', N'created_at', N'createdAt'),
    (N'problem', N'Problem', N'suggested_repair_id', N'suggestedRepair_id'),
    (N'problem', N'Problem', N'match_percent', N'matchPercent'),
    (N'problem', N'Problem', N'estimated_cost', N'estimatedCost'),
    (N'problem', N'Problem', N'estimate_note', N'estimateNote'),
    (N'problem', N'Problem', N'resolved_by_service_id', N'resolvedByService_id'),
    (N'diagnostic_rule', N'DiagnosticRule', N'id', N'id'),
    (N'diagnostic_rule', N'DiagnosticRule', N'code', N'code'),
    (N'diagnostic_rule', N'DiagnosticRule', N'candidate_id', N'candidate_id'),
    (N'diagnostic_rule', N'DiagnosticRule', N'phrase', N'phrase'),
    (N'diagnostic_rule', N'DiagnosticRule', N'weight', N'weight'),
    (N'diagnostic_rule', N'DiagnosticRule', N'active', N'active');

-- Exact-case matching also detects case-only renames on case-insensitive DBs.
DECLARE @IsBefore bit = 1;
DECLARE @IsAfter bit = 1;
IF EXISTS (
    SELECT 1 FROM @Tables m
    WHERE NOT EXISTS (
        SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id
        WHERE s.name=N'dbo' AND t.name COLLATE Latin1_General_100_BIN2=m.BeforeName
    )
) SET @IsBefore=0;
IF EXISTS (
    SELECT 1 FROM @Tables m
    WHERE NOT EXISTS (
        SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id
        WHERE s.name=N'dbo' AND t.name COLLATE Latin1_General_100_BIN2=m.AfterName
    )
) SET @IsAfter=0;
IF EXISTS (
    SELECT 1 FROM @Columns m
    WHERE NOT EXISTS (
        SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id
        JOIN sys.columns c ON c.object_id=t.object_id
        WHERE s.name=N'dbo' AND t.name COLLATE Latin1_General_100_BIN2=m.BeforeTable
          AND c.name COLLATE Latin1_General_100_BIN2=m.BeforeName
    )
) SET @IsBefore=0;
IF EXISTS (
    SELECT 1 FROM @Columns m
    WHERE NOT EXISTS (
        SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id
        JOIN sys.columns c ON c.object_id=t.object_id
        WHERE s.name=N'dbo' AND t.name COLLATE Latin1_General_100_BIN2=m.AfterTable
          AND c.name COLLATE Latin1_General_100_BIN2=m.AfterName
    )
) SET @IsAfter=0;

SELECT DB_NAME() AS CurrentDatabase, @IsBefore AS CompleteOldNaming,
       @IsAfter AS CompleteTargetNaming, @Apply AS ApplyRequested;
SELECT * FROM @Tables ORDER BY BeforeName;
SELECT * FROM @Columns WHERE BeforeName COLLATE Latin1_General_100_BIN2<>AfterName
ORDER BY BeforeTable, BeforeName;

IF @IsAfter=1
BEGIN
    PRINT 'Target names already present. No changes made.';
    RETURN;
END;
IF @IsBefore=0
    THROW 51002, 'Schema differs from this manifest, is partial, or is absent. Inspect; do not recreate it.', 1;

-- Any target object other than the same case-insensitive old object is a collision.
IF EXISTS (
    SELECT 1 FROM @Tables m
    WHERE OBJECT_ID(N'dbo.'+QUOTENAME(m.AfterName)) IS NOT NULL
      AND OBJECT_ID(N'dbo.'+QUOTENAME(m.AfterName))<>OBJECT_ID(N'dbo.'+QUOTENAME(m.BeforeName))
)
    THROW 51003, 'Target name is occupied by another object.', 1;

SELECT DISTINCT OBJECT_SCHEMA_NAME(d.referencing_id) AS RefSchema,
       OBJECT_NAME(d.referencing_id) AS RefObject, o.type_desc
FROM sys.sql_expression_dependencies d JOIN sys.objects o ON o.object_id=d.referencing_id
WHERE d.referenced_id IN (SELECT OBJECT_ID(N'dbo.'+QUOTENAME(BeforeName)) FROM @Tables);

IF @Apply=0
BEGIN
    PRINT 'READ-ONLY PLAN. Review dependencies and use a tested backup/recovery path before enabling Apply.';
    RETURN;
END;
IF @TargetDatabase=N'REPLACE_WITH_ACTUAL_DATABASE' OR DB_NAME()<>@TargetDatabase
    THROW 51004, 'Explicit database confirmation does not match.', 1;
IF @RecoveryVerified=0 OR @ApplicationStopped=0 OR @DependenciesReviewed=0
    THROW 51005, 'Recovery, application stop and dependency review must be explicitly confirmed.', 1;

DECLARE @OriginalObjects TABLE (ObjectId int PRIMARY KEY, AfterName sysname);
INSERT INTO @OriginalObjects
SELECT OBJECT_ID(N'dbo.'+QUOTENAME(BeforeName)), AfterName FROM @Tables;

-- Save physical column IDs/types, not just display names. Renaming must preserve them.
DECLARE @OriginalColumns TABLE (
    ObjectId int, ColumnId int, SystemTypeId int, UserTypeId int,
    MaxLength smallint, PrecisionValue tinyint, ScaleValue tinyint,
    IsNullable bit, IsIdentity bit, IsComputed bit,
    PRIMARY KEY (ObjectId, ColumnId)
);
INSERT INTO @OriginalColumns
SELECT c.object_id,c.column_id,c.system_type_id,c.user_type_id,c.max_length,c.precision,c.scale,
       c.is_nullable,c.is_identity,c.is_computed
FROM sys.columns c JOIN @OriginalObjects o ON o.ObjectId=c.object_id;

DECLARE @OriginalFK TABLE (
    ConstraintId int, Ordinal int, ParentId int, ParentColumn int, ReferencedId int, ReferencedColumn int,
    PRIMARY KEY (ConstraintId, Ordinal)
);
INSERT INTO @OriginalFK
SELECT constraint_object_id,constraint_column_id,parent_object_id,parent_column_id,
       referenced_object_id,referenced_column_id
FROM sys.foreign_key_columns
WHERE parent_object_id IN (SELECT ObjectId FROM @OriginalObjects)
   OR referenced_object_id IN (SELECT ObjectId FROM @OriginalObjects);

DECLARE @OriginalIndexes TABLE (
    ObjectId int, IndexId int, ColumnId int, KeyOrdinal tinyint, IsIncluded bit,
    IsUnique bit, IsPrimary bit, PRIMARY KEY (ObjectId, IndexId, ColumnId)
);
INSERT INTO @OriginalIndexes
SELECT ic.object_id,ic.index_id,ic.column_id,ic.key_ordinal,ic.is_included_column,i.is_unique,i.is_primary_key
FROM sys.index_columns ic JOIN sys.indexes i ON i.object_id=ic.object_id AND i.index_id=ic.index_id
WHERE ic.object_id IN (SELECT ObjectId FROM @OriginalObjects);

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @Before sysname, @After sysname, @Table sysname, @Qualified nvarchar(776);
    DECLARE table_renames CURSOR LOCAL FAST_FORWARD FOR
        SELECT BeforeName,AfterName FROM @Tables ORDER BY BeforeName;
    OPEN table_renames;
    FETCH NEXT FROM table_renames INTO @Before,@After;
    WHILE @@FETCH_STATUS=0
    BEGIN
        SET @Qualified=N'dbo.'+QUOTENAME(@Before);
        EXEC sys.sp_rename @objname=@Qualified,@newname=@After,@objtype=N'OBJECT';
        FETCH NEXT FROM table_renames INTO @Before,@After;
    END;
    CLOSE table_renames;
    DEALLOCATE table_renames;

    DECLARE column_renames CURSOR LOCAL FAST_FORWARD FOR
        SELECT AfterTable,BeforeName,AfterName FROM @Columns
        WHERE BeforeName COLLATE Latin1_General_100_BIN2<>AfterName
        ORDER BY AfterTable,BeforeName;
    OPEN column_renames;
    FETCH NEXT FROM column_renames INTO @Table,@Before,@After;
    WHILE @@FETCH_STATUS=0
    BEGIN
        SET @Qualified=N'dbo.'+QUOTENAME(@Table)+N'.'+QUOTENAME(@Before);
        EXEC sys.sp_rename @objname=@Qualified,@newname=@After,@objtype=N'COLUMN';
        FETCH NEXT FROM column_renames INTO @Table,@Before,@After;
    END;
    CLOSE column_renames;
    DEALLOCATE column_renames;

    IF EXISTS (
        SELECT 1 FROM @OriginalObjects o
        LEFT JOIN sys.tables t ON t.object_id=o.ObjectId
        WHERE t.object_id IS NULL OR t.name COLLATE Latin1_General_100_BIN2<>o.AfterName
    ) THROW 51006, 'A table ID or target name differs after rename.', 1;

    IF EXISTS (
        SELECT 1 FROM @Columns m
        WHERE NOT EXISTS (
            SELECT 1 FROM sys.columns c
            WHERE c.object_id=OBJECT_ID(N'dbo.'+QUOTENAME(m.AfterTable))
              AND c.name COLLATE Latin1_General_100_BIN2=m.AfterName
        )
    ) THROW 51007, 'A target column is missing.', 1;

    IF EXISTS (
        SELECT * FROM @OriginalColumns
        EXCEPT
        SELECT c.object_id,c.column_id,c.system_type_id,c.user_type_id,c.max_length,c.precision,c.scale,
               c.is_nullable,c.is_identity,c.is_computed FROM sys.columns c
    ) THROW 51008, 'Column IDs, types or nullability changed unexpectedly.', 1;

    IF EXISTS (
        SELECT * FROM @OriginalFK
        EXCEPT
        SELECT constraint_object_id,constraint_column_id,parent_object_id,parent_column_id,
               referenced_object_id,referenced_column_id FROM sys.foreign_key_columns
    ) THROW 51009, 'A foreign key mapping changed unexpectedly.', 1;

    IF EXISTS (
        SELECT * FROM @OriginalIndexes
        EXCEPT
        SELECT ic.object_id,ic.index_id,ic.column_id,ic.key_ordinal,ic.is_included_column,
               i.is_unique,i.is_primary_key
        FROM sys.index_columns ic JOIN sys.indexes i
        ON i.object_id=ic.object_id AND i.index_id=ic.index_id
    ) THROW 51010, 'An index or unique-key mapping changed unexpectedly.', 1;

    COMMIT TRANSACTION;
    PRINT 'Names changed. Deploy the matching Java code and validate; never start the old importer.';
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
