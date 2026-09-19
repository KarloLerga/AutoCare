/* AutoCare final audit after professor-model migration. READ ONLY. */
SET NOCOUNT ON;

SELECT DB_NAME() AS database_name;
SELECT COUNT_BIG(*) AS vehicle_variant_count FROM dbo.vehicle_variant;
SELECT COUNT_BIG(*) AS work_definition_count FROM dbo.work_definition;
SELECT COUNT_BIG(*) AS work_price_range_count FROM dbo.work_price_range;
SELECT COUNT_BIG(*) AS problem_count FROM dbo.problem;
SELECT COUNT_BIG(*) AS service_record_count FROM dbo.service_record;
SELECT COUNT_BIG(*) AS service_item_count FROM dbo.service_item;

SELECT category, COUNT_BIG(*) AS work_count
FROM dbo.work_definition
GROUP BY category
ORDER BY category;

SELECT price_class, COUNT_BIG(*) AS price_range_count
FROM dbo.work_price_range
GROUP BY price_class
ORDER BY price_class;

SELECT price_class, COUNT_BIG(*) AS vehicle_count
FROM dbo.vehicle_variant
GROUP BY price_class
ORDER BY price_class;

SELECT
  SUM(CASE WHEN price_class=N'ECONOMY' THEN 1 ELSE 0 END) AS economy,
  SUM(CASE WHEN price_class=N'STANDARD' THEN 1 ELSE 0 END) AS standard,
  SUM(CASE WHEN price_class=N'PREMIUM' THEN 1 ELSE 0 END) AS premium,
  SUM(CASE WHEN price_class=N'PERFORMANCE' THEN 1 ELSE 0 END) AS performance,
  SUM(CASE WHEN price_class=N'EXOTIC' THEN 1 ELSE 0 END) AS exotic
FROM dbo.vehicle_variant;

SELECT catalog_category, COUNT_BIG(*) AS work_count
FROM dbo.work_definition
GROUP BY catalog_category
ORDER BY catalog_category;

SELECT COUNT_BIG(*) AS invalid_price_ranges
FROM dbo.work_price_range
WHERE min_price IS NULL OR max_price IS NULL OR min_price<=0 OR max_price<min_price;

SELECT COUNT_BIG(*) AS maintenance_without_interval
FROM dbo.work_definition
WHERE category=N'MAINTENANCE' AND interval_km IS NULL AND interval_months IS NULL;

SELECT COUNT_BIG(*) AS repair_with_interval
FROM dbo.work_definition
WHERE category=N'REPAIR' AND (interval_km IS NOT NULL OR interval_months IS NOT NULL);

SELECT COUNT_BIG(*) AS variants_without_price_class
FROM dbo.vehicle_variant
WHERE price_class IS NULL;

SELECT COUNT_BIG(*) AS works_without_catalog_category
FROM dbo.work_definition
WHERE catalog_category IS NULL;

SELECT COUNT_BIG(*) AS notes_without_category
FROM dbo.problem
WHERE category IS NULL;

SELECT COUNT_BIG(*) AS works_without_five_price_ranges
FROM (
  SELECT work_id
  FROM dbo.work_price_range
  GROUP BY work_id
  HAVING COUNT(*)<>5
) invalid;

SELECT
  CASE WHEN OBJECT_ID(N'dbo.vehicle_work_rule',N'U') IS NULL THEN 0 ELSE 1 END AS vehicle_work_rule_table_exists,
  CASE WHEN OBJECT_ID(N'dbo.diagnostic_rule',N'U') IS NULL THEN 0 ELSE 1 END AS diagnostic_rule_table_exists,
  CASE WHEN COL_LENGTH(N'dbo.problem',N'suggested_repair_id') IS NULL THEN 0 ELSE 1 END AS suggested_repair_column_exists,
  CASE WHEN COL_LENGTH(N'dbo.problem',N'estimated_cost') IS NULL THEN 0 ELSE 1 END AS estimated_cost_column_exists;

SELECT
    CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant)=30366 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition)=120 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_price_range)=600 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE category=N'MAINTENANCE')=30 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE category=N'REPAIR')=90 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_price_range WHERE min_price IS NULL OR max_price IS NULL OR min_price<=0 OR max_price<min_price)=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE category=N'MAINTENANCE' AND interval_km IS NULL AND interval_months IS NULL)=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE category=N'REPAIR' AND (interval_km IS NOT NULL OR interval_months IS NOT NULL))=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class IS NULL)=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class=N'ECONOMY')=460 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class=N'STANDARD')=18777 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class=N'PREMIUM')=9645 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class=N'PERFORMANCE')=783 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant WHERE price_class=N'EXOTIC')=701 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.work_definition WHERE catalog_category IS NULL)=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM dbo.problem WHERE category IS NULL)=0 THEN 0 ELSE 1 END
  + CASE WHEN (SELECT COUNT_BIG(*) FROM (SELECT work_id FROM dbo.work_price_range GROUP BY work_id HAVING COUNT(*)<>5) p)=0 THEN 0 ELSE 1 END
  + CASE WHEN OBJECT_ID(N'dbo.vehicle_work_rule',N'U') IS NULL THEN 0 ELSE 1 END
  + CASE WHEN OBJECT_ID(N'dbo.diagnostic_rule',N'U') IS NULL THEN 0 ELSE 1 END
  + CASE WHEN COL_LENGTH(N'dbo.problem',N'suggested_repair_id') IS NULL THEN 0 ELSE 1 END
  + CASE WHEN COL_LENGTH(N'dbo.problem',N'estimated_cost') IS NULL THEN 0 ELSE 1 END
  AS final_error_count;
