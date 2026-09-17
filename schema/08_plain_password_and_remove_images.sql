SET NOCOUNT ON;
SET XACT_ABORT ON;

/*
  Final student-model cleanup.
  Default is inspection only.

  Old password hashes are not reversible. If app_user contains rows, setting
  @ResetExistingCredentials = 1 keeps those rows but clears the credential so
  the old account cannot authenticate until recreated/updated for the demo.
*/
DECLARE @Apply bit = 0;
DECLARE @ResetExistingCredentials bit = 0;

DECLARE @DatabaseName sysname = DB_NAME();
DECLARE @UserCount bigint = (SELECT COUNT_BIG(*) FROM dbo.app_user);
DECLARE @VehicleVariantCount bigint = (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant);
DECLARE @WorkCount bigint = (SELECT COUNT_BIG(*) FROM dbo.work_definition);
DECLARE @RuleCount bigint = (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule);
DECLARE @DiagnosticRuleCount bigint = (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule);

SELECT
    @DatabaseName AS database_name,
    @UserCount AS app_user_count,
    @VehicleVariantCount AS vehicle_variant_count,
    @WorkCount AS work_definition_count,
    @RuleCount AS vehicle_work_rule_count,
    @DiagnosticRuleCount AS diagnostic_rule_count,
    COL_LENGTH('dbo.app_user', 'password_hash') AS password_hash_column,
    COL_LENGTH('dbo.app_user', 'password') AS password_column,
    COL_LENGTH('dbo.vehicle_variant', 'image_path') AS image_path_column;

IF @Apply = 0
BEGIN
    PRINT 'Inspection only. Set @Apply = 1 after verifying the target database.';
    RETURN;
END;

IF @UserCount > 0 AND @ResetExistingCredentials = 0
BEGIN
    THROW 50001, 'Existing users found. Set @ResetExistingCredentials = 1 only when credential invalidation is intended.', 1;
END;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.app_user', 'password') IS NULL
       AND COL_LENGTH('dbo.app_user', 'password_hash') IS NOT NULL
    BEGIN
        EXEC sys.sp_rename
            @objname = N'dbo.app_user.password_hash',
            @newname = N'password',
            @objtype = N'COLUMN';
    END;

    IF @ResetExistingCredentials = 1
       AND COL_LENGTH('dbo.app_user', 'password') IS NOT NULL
    BEGIN
        EXEC(
            N'ALTER TABLE dbo.app_user ALTER COLUMN [password] nvarchar(255) NULL;'
                + N' UPDATE dbo.app_user SET [password] = NULL;');
    END;

    IF COL_LENGTH('dbo.vehicle_variant', 'image_path') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.vehicle_variant DROP COLUMN image_path;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
    BEGIN
        ROLLBACK TRANSACTION;
    END;

    THROW;
END CATCH;

SELECT
    DB_NAME() AS database_name,
    (SELECT COUNT_BIG(*) FROM dbo.app_user) AS app_user_count,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS vehicle_variant_count,
    (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS work_definition_count,
    (SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS vehicle_work_rule_count,
    (SELECT COUNT_BIG(*) FROM dbo.diagnostic_rule) AS diagnostic_rule_count,
    COL_LENGTH('dbo.app_user', 'password_hash') AS password_hash_column,
    COL_LENGTH('dbo.app_user', 'password') AS password_column,
    COL_LENGTH('dbo.vehicle_variant', 'image_path') AS image_path_column;
