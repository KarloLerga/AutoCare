-- AFTER naming migration. Read-only. Counts are diagnostics, not permission to rewrite data.
SELECT COUNT_BIG(*) AS InvalidScheduleRows
FROM dbo.VehicleWorkRule r JOIN dbo.WorkDefinition w ON w.id=r.work_id
WHERE r.scheduleKind IS NULL
   OR (r.scheduleKind='FIXED' AND r.intervalKm IS NULL AND r.intervalMonths IS NULL)
   OR (r.scheduleKind<>'FIXED' AND (r.intervalKm IS NOT NULL OR r.intervalMonths IS NOT NULL))
   OR (w.category='REPAIR' AND r.scheduleKind='FIXED');

SELECT COUNT_BIG(*) AS InvalidActiveVehicles
FROM dbo.AppUser u LEFT JOIN dbo.Vehicle v ON v.id=u.activeVehicle_id
WHERE u.activeVehicle_id IS NULL OR v.id IS NULL OR v.owner_id<>u.id;

SELECT COUNT_BIG(*) AS InvalidResolvedProblems
FROM dbo.Problem p LEFT JOIN dbo.ServiceRecord s ON s.id=p.resolvedByService_id
WHERE (p.status='OPEN' AND p.resolvedByService_id IS NOT NULL)
   OR (p.status='RESOLVED' AND (s.id IS NULL OR s.vehicle_id<>p.vehicle_id));

-- Run once on the isolated copy for before/after comparison using matching old names before migration.
SELECT COUNT_BIG(*) AS ItemCount,COUNT(actualPrice) AS KnownPrices,
       SUM(actualPrice) AS ActualPaidTotal FROM dbo.ServiceItem;

SELECT variant_id,work_id,COUNT_BIG(*) AS Duplicates FROM dbo.VehicleWorkRule
GROUP BY variant_id,work_id HAVING COUNT_BIG(*)>1;

-- A price model tag does not prove accuracy. Never replace unknown intervals with arbitrary numbers.
-- Audit all table counts and selected unchanged catalogue codes on the test copy, not repeated full seeding.
