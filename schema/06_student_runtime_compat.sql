-- AutoCare: minimal compatibility changes for the existing student Azure SQL database.
-- No table rename. No rebuild. No data deletion.
-- Run once with @Apply = 0, verify the output and database name, then set @Apply = 1.

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @Apply bit = 0;
DECLARE @ExpectedDatabase sysname = N'REPLACE_WITH_ACTUAL_DATABASE';

IF DB_NAME() IN (N'master', N'model', N'tempdb', N'msdb')
    THROW 51040, 'Select the AutoCare application database.', 1;

DECLARE @WorkDefinitionExists bit =
    CASE WHEN OBJECT_ID(N'dbo.work_definition', N'U') IS NULL THEN 0 ELSE 1 END;

DECLARE @ServiceRecordExists bit =
    CASE WHEN OBJECT_ID(N'dbo.service_record', N'U') IS NULL THEN 0 ELSE 1 END;

DECLARE @DefaultKmExists bit =
    CASE WHEN COL_LENGTH(N'dbo.work_definition', N'default_interval_km') IS NULL THEN 0 ELSE 1 END;

DECLARE @DefaultMonthsExists bit =
    CASE WHEN COL_LENGTH(N'dbo.work_definition', N'default_interval_months') IS NULL THEN 0 ELSE 1 END;

DECLARE @LegacyRequestKeyExists bit =
    CASE WHEN COL_LENGTH(N'dbo.service_record', N'request_key') IS NULL THEN 0 ELSE 1 END;

DECLARE @LegacyRequestKeyHasDefault bit = 0;

IF @LegacyRequestKeyExists = 1
BEGIN
    SELECT @LegacyRequestKeyHasDefault =
        CASE WHEN dc.object_id IS NULL THEN 0 ELSE 1 END
    FROM sys.columns c
    LEFT JOIN sys.default_constraints dc
        ON dc.parent_object_id = c.object_id
       AND dc.parent_column_id = c.column_id
    WHERE c.object_id = OBJECT_ID(N'dbo.service_record')
      AND c.name = N'request_key';
END;

SELECT
    DB_NAME() AS CurrentDatabase,
    @WorkDefinitionExists AS WorkDefinitionExists,
    @ServiceRecordExists AS ServiceRecordExists,
    @DefaultKmExists AS DefaultIntervalKmExists,
    @DefaultMonthsExists AS DefaultIntervalMonthsExists,
    @LegacyRequestKeyExists AS LegacyRequestKeyExists,
    @LegacyRequestKeyHasDefault AS LegacyRequestKeyHasDefault,
    @Apply AS ApplyRequested;

IF @Apply = 0
BEGIN
    PRINT 'READ-ONLY CHECK COMPLETE. Set the exact database name and @Apply = 1 only after review.';
    RETURN;
END;

IF @ExpectedDatabase = N'REPLACE_WITH_ACTUAL_DATABASE'
    THROW 51041, 'Set @ExpectedDatabase before applying changes.', 1;

IF DB_NAME() <> @ExpectedDatabase
    THROW 51042, 'Current database does not match @ExpectedDatabase.', 1;

IF @WorkDefinitionExists = 0 OR @ServiceRecordExists = 0
    THROW 51043, 'Expected snake_case AutoCare tables are missing. Do not guess or create parallel tables.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.work_definition', N'default_interval_km') IS NULL
    BEGIN
        ALTER TABLE dbo.work_definition
        ADD default_interval_km int NULL;
    END;

    IF COL_LENGTH(N'dbo.work_definition', N'default_interval_months') IS NULL
    BEGIN
        ALTER TABLE dbo.work_definition
        ADD default_interval_months int NULL;
    END;

    IF COL_LENGTH(N'dbo.service_record', N'request_key') IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM sys.columns c
            JOIN sys.default_constraints dc
              ON dc.parent_object_id = c.object_id
             AND dc.parent_column_id = c.column_id
            WHERE c.object_id = OBJECT_ID(N'dbo.service_record')
              AND c.name = N'request_key'
       )
    BEGIN
        ALTER TABLE dbo.service_record
        ADD CONSTRAINT DF_autocare_service_record_request_key
        DEFAULT (CONVERT(nvarchar(36), NEWID())) FOR request_key;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

PRINT 'Minimal AutoCare runtime compatibility changes applied.';
