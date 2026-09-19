#!/usr/bin/env python3
"""Validate final AutoCare professor-approved catalogue seed files."""
from __future__ import annotations

import csv
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKS = ROOT / "data_model/work_catalog_final.csv"
PRICES = ROOT / "data_model/work_price_ranges.csv"
VEHICLES = ROOT / "data_model/vehicle_price_classes.csv"
SUMMARY = ROOT / "data_model/professor_catalog_validation.json"

PRICE_CLASSES = ["ECONOMY", "STANDARD", "PREMIUM", "PERFORMANCE", "EXOTIC"]
WORK_CATEGORIES = {"MAINTENANCE", "REPAIR"}


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def main() -> int:
    errors: list[str] = []

    with WORKS.open(encoding="utf-8", newline="") as handle:
        works = list(csv.DictReader(handle))
    with PRICES.open(encoding="utf-8", newline="") as handle:
        prices = list(csv.DictReader(handle))
    with VEHICLES.open(encoding="utf-8", newline="") as handle:
        vehicles = list(csv.DictReader(handle))

    work_codes = [row["code"] for row in works]
    if len(works) != 120:
        fail(errors, f"Expected 120 works, got {len(works)}")
    if len(set(work_codes)) != len(work_codes):
        fail(errors, "Duplicate work codes")

    work_by_code = {row["code"]: row for row in works}
    work_category_counts = Counter(row["work_category"] for row in works)
    if work_category_counts != Counter({"REPAIR": 90, "MAINTENANCE": 30}):
        fail(errors, f"Unexpected work category counts: {dict(work_category_counts)}")

    for row in works:
        code = row["code"]
        category = row["work_category"]
        if category not in WORK_CATEGORIES:
            fail(errors, f"Unknown work category {category}: {code}")
        km = row["interval_km"].strip()
        months = row["interval_months"].strip()
        if category == "MAINTENANCE" and not km and not months:
            fail(errors, f"Maintenance without interval: {code}")
        if category == "REPAIR" and (km or months):
            fail(errors, f"Repair with preventive interval: {code}")
        for label, value in (("interval_km", km), ("interval_months", months)):
            if value and int(value) <= 0:
                fail(errors, f"Non-positive {label}: {code}")
        if not row["catalog_category"].strip():
            fail(errors, f"Work without catalogue category: {code}")

    if len(prices) != 600:
        fail(errors, f"Expected 600 price ranges, got {len(prices)}")

    seen_price_keys: set[tuple[str, str]] = set()
    prices_by_work: dict[str, dict[str, tuple[float, float]]] = defaultdict(dict)
    for row in prices:
        code = row["work_code"]
        price_class = row["price_class"]
        key = (code, price_class)
        if key in seen_price_keys:
            fail(errors, f"Duplicate price range: {code}/{price_class}")
        seen_price_keys.add(key)
        if code not in work_by_code:
            fail(errors, f"Price for unknown work: {code}")
        if price_class not in PRICE_CLASSES:
            fail(errors, f"Unknown price class: {price_class}")
        min_price = float(row["min_price_eur"])
        max_price = float(row["max_price_eur"])
        if min_price <= 0 or max_price < min_price:
            fail(errors, f"Invalid price range: {code}/{price_class}")
        prices_by_work[code][price_class] = (min_price, max_price)

    for code in work_codes:
        available = prices_by_work.get(code, {})
        if set(available) != set(PRICE_CLASSES):
            fail(errors, f"Work does not have all five price classes: {code}")
            continue
        midpoints = []
        for price_class in PRICE_CLASSES:
            low, high = available[price_class]
            midpoints.append((low + high) / 2.0)
        if any(midpoints[index] > midpoints[index + 1] for index in range(4)):
            fail(errors, f"Price classes decrease unexpectedly: {code}")

    vehicle_codes = [row["variant_code"] for row in vehicles]
    if len(vehicles) != 30366:
        fail(errors, f"Expected 30366 vehicle price classes, got {len(vehicles)}")
    if len(set(vehicle_codes)) != len(vehicle_codes):
        fail(errors, "Duplicate vehicle variant codes")
    vehicle_class_counts = Counter()
    for row in vehicles:
        price_class = row["price_class"]
        if price_class not in PRICE_CLASSES:
            fail(errors, f"Unknown vehicle price class: {price_class}")
        vehicle_class_counts[price_class] += 1

    result = {
        "vehicle_variants": len(vehicles),
        "work_definitions": len(works),
        "maintenance_works": work_category_counts["MAINTENANCE"],
        "repair_works": work_category_counts["REPAIR"],
        "price_ranges": len(prices),
        "vehicle_price_class_counts": dict(sorted(vehicle_class_counts.items())),
        "errors": len(errors),
        "first_errors": errors[:50],
    }
    SUMMARY.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
