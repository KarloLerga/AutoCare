/*
  AutoCare - SQL Server migration for AppUser.activeVehicle OneToOne semantics.

  The project uses hibernate.hbm2ddl.auto=none, so the JPA annotation change does not modify
  the existing Azure SQL schema automatically.

  This migration was checked against dbo.app_user and dbo.vehicle. It clears invalid or duplicate
  active-vehicle references before adding the filtered unique index. A user may have no active
  vehicle (NULL), but a concrete vehicle may be active for at most one user.
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

/* Clear active-vehicle choices that do not belong to the account selecting them. */
UPDATE appUser
SET active_vehicle_id = NULL
FROM dbo.app_user AS appUser
LEFT JOIN dbo.vehicle AS vehicle
  ON vehicle.id = appUser.active_vehicle_id
 AND vehicle.owner_id = appUser.id
WHERE appUser.active_vehicle_id IS NOT NULL
  AND vehicle.id IS NULL;

/* If a vehicle is selected by multiple accounts, keep the first selection. */
;WITH rankedActiveVehicles AS (
  SELECT
    id,
    active_vehicle_id,
    ROW_NUMBER() OVER (
      PARTITION BY active_vehicle_id
      ORDER BY id
    ) AS rowNumber
  FROM dbo.app_user
  WHERE active_vehicle_id IS NOT NULL
)
UPDATE appUser
SET active_vehicle_id = NULL
FROM dbo.app_user AS appUser
INNER JOIN rankedActiveVehicles AS ranked
  ON ranked.id = appUser.id
WHERE ranked.rowNumber > 1;

/* Allow multiple NULL values while requiring every selected vehicle ID to be unique. */
IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.app_user')
    AND name = N'UX_app_user_active_vehicle'
)
BEGIN
  CREATE UNIQUE INDEX UX_app_user_active_vehicle
    ON dbo.app_user(active_vehicle_id)
    WHERE active_vehicle_id IS NOT NULL;
END;

COMMIT TRANSACTION;
