#!/usr/bin/env python3
"""Generate the one-time Azure SQL migration to the professor-approved model."""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKS = ROOT / "data_model/work_catalog_final.csv"
PRICES = ROOT / "data_model/work_price_ranges.csv"
CLASSES = ROOT / "data_model/vehicle_price_classes.csv"
OUT = ROOT / "schema/11_professor_model.sql"


def n(value: str) -> str:
    return "N'" + value.replace("'", "''") + "'"


def sql_value(value: str) -> str:
    return "NULL" if value == "" else value


def chunks(values, size=500):
    for index in range(0, len(values), size):
        yield values[index:index + size]


def main() -> None:
    with WORKS.open(encoding="utf-8", newline="") as handle:
        works = list(csv.DictReader(handle))
    with PRICES.open(encoding="utf-8", newline="") as handle:
        prices = list(csv.DictReader(handle))
    with CLASSES.open(encoding="utf-8", newline="") as handle:
        classes = list(csv.DictReader(handle))

    assert len(works) == 120
    assert len(prices) == 600
    assert len(classes) == 30366

    lines = []
    a = lines.append
    a("-- AutoCare migration after professor review: Notes + searchable Catalog + actual Services.")
    a("-- Default is READ ONLY. SqlSeedTool changes @Apply to 1 only with explicit --apply.")
    a("SET NOCOUNT ON;")
    a("DECLARE @Apply bit = 0;")
    a("")
    a("IF DB_NAME() IN (N'master',N'model',N'msdb',N'tempdb')")
    a("  THROW 51200, 'Sistemska baza nije dopuštena za AutoCare migraciju.', 1;")
    a("")
    a("DECLARE @OldVariantRules bigint=0;")
    a("DECLARE @CurrentPriceRanges bigint=0;")
    a("IF OBJECT_ID(N'dbo.vehicle_work_rule',N'U') IS NOT NULL")
    a("  EXEC sys.sp_executesql N'SELECT @Count=COUNT_BIG(*) FROM dbo.vehicle_work_rule', N'@Count bigint OUTPUT', @Count=@OldVariantRules OUTPUT;")
    a("IF OBJECT_ID(N'dbo.work_price_range',N'U') IS NOT NULL")
    a("  EXEC sys.sp_executesql N'SELECT @Count=COUNT_BIG(*) FROM dbo.work_price_range', N'@Count bigint OUTPUT', @Count=@CurrentPriceRanges OUTPUT;")
    a("SELECT DB_NAME() AS database_name,")
    a("  (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS vehicle_variants,")
    a("  (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS work_definitions,")
    a("  @OldVariantRules AS old_variant_rules,")
    a("  @CurrentPriceRanges AS current_price_ranges;")
    a("")
    a("IF @Apply = 0 RETURN;")
    a("")
    a("BEGIN TRY")
    a("  BEGIN TRANSACTION;")
    a("")
    a("  IF (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) <> 30366")
    a("    THROW 51201, 'Očekuje se 30366 varijanti vozila.', 1;")
    a("  IF (SELECT COUNT_BIG(*) FROM dbo.work_definition) <> 120")
    a("    THROW 51202, 'Očekuje se 120 finalnih standardnih zahvata.', 1;")
    a("")
    a("  IF COL_LENGTH(N'dbo.vehicle_variant',N'price_class') IS NULL")
    a("    ALTER TABLE dbo.vehicle_variant ADD price_class nvarchar(32) NULL;")
    a("  UPDATE dbo.vehicle_variant SET price_class=N'STANDARD';")
    a("")
    a("  CREATE TABLE #VariantClass(code nvarchar(80) NOT NULL PRIMARY KEY, price_class nvarchar(32) NOT NULL);")
    non_standard = [row for row in classes if row["price_class"] != "STANDARD"]
    for batch in chunks(non_standard):
        a("  INSERT INTO #VariantClass(code,price_class) VALUES")
        a(",\n".join("    (" + n(row["variant_code"]) + "," + n(row["price_class"]) + ")" for row in batch) + ";")
    a("  UPDATE variant SET price_class=classes.price_class")
    a("  FROM dbo.vehicle_variant variant JOIN #VariantClass classes ON classes.code=variant.code;")
    a("  IF (SELECT COUNT(*) FROM dbo.vehicle_variant WHERE price_class=N'ECONOMY') <> 460")
    a("    THROW 51203, 'Ne odgovara broj ECONOMY vozila.', 1;")
    a("  IF (SELECT COUNT(*) FROM dbo.vehicle_variant WHERE price_class=N'STANDARD') <> 18777")
    a("    THROW 51204, 'Ne odgovara broj STANDARD vozila.', 1;")
    a("  IF (SELECT COUNT(*) FROM dbo.vehicle_variant WHERE price_class=N'PREMIUM') <> 9645")
    a("    THROW 51205, 'Ne odgovara broj PREMIUM vozila.', 1;")
    a("  IF (SELECT COUNT(*) FROM dbo.vehicle_variant WHERE price_class=N'PERFORMANCE') <> 783")
    a("    THROW 51206, 'Ne odgovara broj PERFORMANCE vozila.', 1;")
    a("  IF (SELECT COUNT(*) FROM dbo.vehicle_variant WHERE price_class=N'EXOTIC') <> 701")
    a("    THROW 51207, 'Ne odgovara broj EXOTIC vozila.', 1;")
    a("  ALTER TABLE dbo.vehicle_variant ALTER COLUMN price_class nvarchar(32) NOT NULL;")
    a("")
    a("  IF COL_LENGTH(N'dbo.work_definition',N'catalog_category') IS NULL")
    a("    ALTER TABLE dbo.work_definition ADD catalog_category nvarchar(40) NULL;")
    a("  IF COL_LENGTH(N'dbo.work_definition',N'interval_km') IS NULL")
    a("    ALTER TABLE dbo.work_definition ADD interval_km int NULL;")
    a("  IF COL_LENGTH(N'dbo.work_definition',N'interval_months') IS NULL")
    a("    ALTER TABLE dbo.work_definition ADD interval_months int NULL;")
    a("")
    a("  CREATE TABLE #WorkCatalog(code nvarchar(80) NOT NULL PRIMARY KEY, display_name nvarchar(160) NOT NULL, work_category nvarchar(32) NOT NULL, catalog_category nvarchar(40) NOT NULL, interval_km int NULL, interval_months int NULL);")
    a("  INSERT INTO #WorkCatalog(code,display_name,work_category,catalog_category,interval_km,interval_months) VALUES")
    a(",\n".join(
        "    (" + ",".join([
            n(row["code"]), n(row["name"]), n(row["work_category"]), n(row["catalog_category"]),
            sql_value(row["interval_km"]), sql_value(row["interval_months"])
        ]) + ")" for row in works
    ) + ";")
    a("  IF (SELECT COUNT(*) FROM #WorkCatalog) <> 120 THROW 51208, 'Nevaljan broj radova u seedu.', 1;")
    a("  IF (SELECT COUNT(*) FROM dbo.work_definition work JOIN #WorkCatalog seed ON seed.code=work.code) <> 120")
    a("    THROW 51209, 'Kodovi radova u bazi ne odgovaraju finalnom katalogu.', 1;")
    a("  UPDATE work SET name=seed.display_name, category=seed.work_category, catalog_category=seed.catalog_category, interval_km=seed.interval_km, interval_months=seed.interval_months")
    a("  FROM dbo.work_definition work JOIN #WorkCatalog seed ON seed.code=work.code;")
    a("  ALTER TABLE dbo.work_definition ALTER COLUMN catalog_category nvarchar(40) NOT NULL;")
    a("")
    a("  IF COL_LENGTH(N'dbo.problem',N'category') IS NULL")
    a("    ALTER TABLE dbo.problem ADD category nvarchar(32) NULL;")
    a("  UPDATE dbo.problem SET category=N'OTHER' WHERE category IS NULL;")
    a("  ALTER TABLE dbo.problem ALTER COLUMN category nvarchar(32) NOT NULL;")
    a("")
    a("  DECLARE @Sql nvarchar(max)=N'';")
    a("  IF COL_LENGTH(N'dbo.problem',N'suggested_repair_id') IS NOT NULL")
    a("  BEGIN")
    a("    SELECT @Sql=@Sql+N'ALTER TABLE dbo.problem DROP CONSTRAINT '+QUOTENAME(fk.name)+N';'")
    a("    FROM sys.foreign_keys fk")
    a("    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id=fk.object_id")
    a("    WHERE fk.parent_object_id=OBJECT_ID(N'dbo.problem')")
    a("      AND fkc.parent_column_id=COLUMNPROPERTY(OBJECT_ID(N'dbo.problem'),N'suggested_repair_id','ColumnId');")
    a("    IF @Sql<>N'' EXEC sys.sp_executesql @Sql;")
    a("    ALTER TABLE dbo.problem DROP COLUMN suggested_repair_id;")
    a("  END;")
    a("  IF COL_LENGTH(N'dbo.problem',N'estimated_cost') IS NOT NULL")
    a("    ALTER TABLE dbo.problem DROP COLUMN estimated_cost;")
    a("")
    a("  IF OBJECT_ID(N'dbo.work_price_range',N'U') IS NOT NULL DROP TABLE dbo.work_price_range;")
    a("  CREATE TABLE dbo.work_price_range(")
    a("    id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY,")
    a("    work_id bigint NOT NULL,")
    a("    price_class nvarchar(32) NOT NULL,")
    a("    min_price decimal(12,2) NOT NULL,")
    a("    max_price decimal(12,2) NOT NULL,")
    a("    CONSTRAINT fk_work_price_range_work FOREIGN KEY(work_id) REFERENCES dbo.work_definition(id),")
    a("    CONSTRAINT uq_work_price_range_work_class UNIQUE(work_id,price_class)")
    a("  );")
    a("")
    a("  CREATE TABLE #PriceRange(work_code nvarchar(80) NOT NULL, price_class nvarchar(32) NOT NULL, min_price decimal(12,2) NOT NULL, max_price decimal(12,2) NOT NULL);")
    a("  INSERT INTO #PriceRange(work_code,price_class,min_price,max_price) VALUES")
    a(",\n".join(
        "    (" + ",".join([n(row["work_code"]), n(row["price_class"]), row["min_price_eur"], row["max_price_eur"]]) + ")"
        for row in prices
    ) + ";")
    a("  INSERT INTO dbo.work_price_range(work_id,price_class,min_price,max_price)")
    a("  SELECT work.id,price.price_class,price.min_price,price.max_price")
    a("  FROM #PriceRange price JOIN dbo.work_definition work ON work.code=price.work_code;")
    a("  IF (SELECT COUNT(*) FROM dbo.work_price_range) <> 600")
    a("    THROW 51210, 'Očekuje se 600 raspona cijena.', 1;")
    a("")
    a("  IF OBJECT_ID(N'dbo.vehicle_work_rule',N'U') IS NOT NULL")
    a("  BEGIN")
    a("    IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE referenced_object_id=OBJECT_ID(N'dbo.vehicle_work_rule'))")
    a("      THROW 51211, 'Druga tablica referencira staru vehicle_work_rule tablicu.', 1;")
    a("    DROP TABLE dbo.vehicle_work_rule;")
    a("  END;")
    a("  IF OBJECT_ID(N'dbo.diagnostic_rule',N'U') IS NOT NULL DROP TABLE dbo.diagnostic_rule;")
    a("")
    a("  IF EXISTS (SELECT 1 FROM dbo.work_definition WHERE category=N'MAINTENANCE' AND interval_km IS NULL AND interval_months IS NULL)")
    a("    THROW 51212, 'Maintenance rad bez intervala.', 1;")
    a("  IF EXISTS (SELECT 1 FROM dbo.work_definition WHERE category=N'REPAIR' AND (interval_km IS NOT NULL OR interval_months IS NOT NULL))")
    a("    THROW 51213, 'Repair rad ne smije imati preventivni interval.', 1;")
    a("  IF EXISTS (SELECT 1 FROM dbo.work_price_range WHERE min_price<=0 OR max_price<min_price)")
    a("    THROW 51214, 'Nevaljan raspon cijene.', 1;")
    a("")
    a("  COMMIT TRANSACTION;")
    a("END TRY")
    a("BEGIN CATCH")
    a("  IF XACT_STATE()<>0 ROLLBACK TRANSACTION;")
    a("  THROW;")
    a("END CATCH;")
    a("")
    a("SELECT")
    a("  (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS vehicle_variants,")
    a("  (SELECT COUNT_BIG(*) FROM dbo.work_definition) AS work_definitions,")
    a("  (SELECT COUNT_BIG(*) FROM dbo.work_price_range) AS price_ranges,")
    a("  CASE WHEN OBJECT_ID(N'dbo.vehicle_work_rule',N'U') IS NULL THEN 0 ELSE 1 END AS old_variant_rule_table_exists,")
    a("  CASE WHEN OBJECT_ID(N'dbo.diagnostic_rule',N'U') IS NULL THEN 0 ELSE 1 END AS diagnostic_rule_table_exists,")
    a("  (SELECT COUNT_BIG(*) FROM dbo.problem WHERE category IS NULL) AS notes_without_category;")

    with OUT.open("w", encoding="utf-8", newline="") as handle:
        handle.write("\n".join(lines) + "\n")
    print(f"wrote {OUT} ({OUT.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
