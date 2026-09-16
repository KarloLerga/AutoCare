-- AutoCare V2: nullable WorkDefinition defaults. Azure SQL Database / SQL Server.
-- REVIEW TEMPLATE: not executed against a real SQL Server during this pass.
-- The default is read-only. Apply only to an isolated, backed-up target after the
-- reviewed naming migration and after Hibernate/JPA metadata has been checked.
-- Existing rows may remain NULL; no data rewrite or database reset is needed.
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET LOCK_TIMEOUT 15000;

DECLARE @Apply bit = 0;
DECLARE @TargetDatabase sysname = N'REPLACE_WITH_ACTUAL_DATABASE';
DECLARE @RecoveryVerified bit = 0;
DECLARE @ApplicationStopped bit = 0;
DECLARE @DependenciesReviewed bit = 0;

IF @@TRANCOUNT <> 0
    THROW 51020, 'Use a session without an existing transaction.', 1;
IF DB_NAME() IN (N'master', N'model', N'tempdb', N'msdb')
    THROW 51021, 'Select the application database, not a system database.', 1;

DECLARE @WorkDefinitionObjectId int = OBJECT_ID(N'dbo.WorkDefinition', N'U');
DECLARE @WorkDefinitionExists bit =
    CASE WHEN @WorkDefinitionObjectId IS NULL THEN 0 ELSE 1 END;
DECLARE @KmExists bit =
    CASE WHEN COL_LENGTH(N'dbo.WorkDefinition', N'defaultIntervalKm') IS NULL
         THEN 0 ELSE 1 END;
DECLARE @MonthsExists bit =
    CASE WHEN COL_LENGTH(N'dbo.WorkDefinition', N'defaultIntervalMonths') IS NULL
         THEN 0 ELSE 1 END;

SELECT DB_NAME() AS CurrentDatabase,
       @WorkDefinitionExists AS WorkDefinitionExists,
       @KmExists AS DefaultIntervalKmExists,
       @MonthsExists AS DefaultIntervalMonthsExists,
       @Apply AS ApplyRequested;

IF @Apply = 0
BEGIN
    PRINT 'READ-ONLY PLAN. Review the target, backup and dependencies before enabling Apply.';
    RETURN;
END;

IF @TargetDatabase = N'REPLACE_WITH_ACTUAL_DATABASE'
   OR DB_NAME() <> @TargetDatabase
    THROW 51022, 'Explicit database confirmation does not match.', 1;
IF @WorkDefinitionExists = 0
    THROW 51023, 'dbo.WorkDefinition is missing; inspect the schema instead of recreating it.', 1;
IF @RecoveryVerified = 0
   OR @ApplicationStopped = 0
   OR @DependenciesReviewed = 0
    THROW 51024, 'Recovery, application stop and dependency review must be explicitly confirmed.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.WorkDefinition', N'defaultIntervalKm') IS NULL
        ALTER TABLE dbo.WorkDefinition ADD defaultIntervalKm int NULL;

    IF COL_LENGTH(N'dbo.WorkDefinition', N'defaultIntervalMonths') IS NULL
        ALTER TABLE dbo.WorkDefinition ADD defaultIntervalMonths int NULL;

    COMMIT TRANSACTION;
    PRINT 'V2 nullable WorkDefinition interval columns are present.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
