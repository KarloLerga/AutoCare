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
    (N'AppUser', N'app_user'),
    (N'VehicleVariant', N'vehicle_variant'),
    (N'Vehicle', N'vehicle'),
    (N'WorkDefinition', N'work_definition'),
    (N'VehicleWorkRule', N'vehicle_work_rule'),
    (N'ServiceRecord', N'service_record'),
    (N'ServiceItem', N'service_item'),
    (N'Problem', N'problem'),
    (N'DiagnosticRule', N'diagnostic_rule');

DECLARE @Columns TABLE (
    BeforeTable sysname, AfterTable sysname, BeforeName sysname, AfterName sysname,
    PRIMARY KEY (BeforeTable, BeforeName)
);
INSERT INTO @Columns VALUES
    (N'AppUser', N'app_user', N'id', N'id'),
    (N'AppUser', N'app_user', N'version', N'version'),
    (N'AppUser', N'app_user', N'name', N'name'),
    (N'AppUser', N'app_user', N'email', N'email'),
    (N'AppUser', N'app_user', N'passwordHash', N'password_hash'),
    (N'AppUser', N'app_user', N'activeVehicle_id', N'active_vehicle_id'),
    (N'VehicleVariant', N'vehicle_variant', N'id', N'id'),
    (N'VehicleVariant', N'vehicle_variant', N'code', N'code'),
    (N'VehicleVariant', N'vehicle_variant', N'make', N'make'),
    (N'VehicleVariant', N'vehicle_variant', N'model', N'model'),
    (N'VehicleVariant', N'vehicle_variant', N'generation', N'generation'),
    (N'VehicleVariant', N'vehicle_variant', N'engineLabel', N'engine_label'),
    (N'VehicleVariant', N'vehicle_variant', N'bodyType', N'body_type'),
    (N'VehicleVariant', N'vehicle_variant', N'fuelType', N'fuel_type'),
    (N'VehicleVariant', N'vehicle_variant', N'powerHp', N'power_hp'),
    (N'VehicleVariant', N'vehicle_variant', N'transmission', N'transmission'),
    (N'VehicleVariant', N'vehicle_variant', N'yearFrom', N'year_from'),
    (N'VehicleVariant', N'vehicle_variant', N'yearTo', N'year_to'),
    (N'VehicleVariant', N'vehicle_variant', N'imagePath', N'image_path'),
    (N'Vehicle', N'vehicle', N'id', N'id'),
    (N'Vehicle', N'vehicle', N'version', N'version'),
    (N'Vehicle', N'vehicle', N'owner_id', N'owner_id'),
    (N'Vehicle', N'vehicle', N'variant_id', N'variant_id'),
    (N'Vehicle', N'vehicle', N'productionYear', N'production_year'),
    (N'Vehicle', N'vehicle', N'currentMileage', N'current_mileage'),
    (N'WorkDefinition', N'work_definition', N'id', N'id'),
    (N'WorkDefinition', N'work_definition', N'code', N'code'),
    (N'WorkDefinition', N'work_definition', N'name', N'name'),
    (N'WorkDefinition', N'work_definition', N'category', N'category'),
    (N'WorkDefinition', N'work_definition', N'defaultEstimatedPrice', N'default_estimated_price'),
    (N'WorkDefinition', N'work_definition', N'estimateNote', N'estimate_note'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'id', N'id'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'variant_id', N'variant_id'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'work_id', N'work_id'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'intervalKm', N'interval_km'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'scheduleKind', N'schedule_kind'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'intervalMonths', N'interval_months'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'estimatedPrice', N'estimated_price'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'intervalSource', N'interval_source'),
    (N'VehicleWorkRule', N'vehicle_work_rule', N'estimateNote', N'estimate_note'),
    (N'ServiceRecord', N'service_record', N'id', N'id'),
    (N'ServiceRecord', N'service_record', N'vehicle_id', N'vehicle_id'),
    (N'ServiceRecord', N'service_record', N'serviceDate', N'service_date'),
    (N'ServiceRecord', N'service_record', N'mileage', N'mileage'),
    (N'ServiceRecord', N'service_record', N'note', N'note'),
    (N'ServiceItem', N'service_item', N'id', N'id'),
    (N'ServiceItem', N'service_item', N'serviceRecord_id', N'service_record_id'),
    (N'ServiceItem', N'service_item', N'work_id', N'work_id'),
    (N'ServiceItem', N'service_item', N'actualPrice', N'actual_price'),
    (N'Problem', N'problem', N'id', N'id'),
    (N'Problem', N'problem', N'version', N'version'),
    (N'Problem', N'problem', N'vehicle_id', N'vehicle_id'),
    (N'Problem', N'problem', N'requestKey', N'request_key'),
    (N'Problem', N'problem', N'description', N'description'),
    (N'Problem', N'problem', N'status', N'status'),
    (N'Problem', N'problem', N'createdAt', N'created_at'),
    (N'Problem', N'problem', N'suggestedRepair_id', N'suggested_repair_id'),
    (N'Problem', N'problem', N'matchPercent', N'match_percent'),
    (N'Problem', N'problem', N'estimatedCost', N'estimated_cost'),
    (N'Problem', N'problem', N'estimateNote', N'estimate_note'),
    (N'Problem', N'problem', N'resolvedByService_id', N'resolved_by_service_id'),
    (N'DiagnosticRule', N'diagnostic_rule', N'id', N'id'),
    (N'DiagnosticRule', N'diagnostic_rule', N'code', N'code'),
    (N'DiagnosticRule', N'diagnostic_rule', N'candidate_id', N'candidate_id'),
    (N'DiagnosticRule', N'diagnostic_rule', N'phrase', N'phrase'),
    (N'DiagnosticRule', N'diagnostic_rule', N'weight', N'weight'),
    (N'DiagnosticRule', N'diagnostic_rule', N'active', N'active');

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
