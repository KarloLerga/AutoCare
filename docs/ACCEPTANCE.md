# Acceptance checklist - actual execution required
Tick an item only after the actual scenario is executed. Source inspection is not equivalent to execution. Use an isolated database for destructive tests.

## Build and configuration
- Java, javac, Maven JAVA_HOME and VS Code all select Java 25. UTF-8 sources compile without preview features.
- Real pinned dependencies resolve and clean verify succeeds. No substitute APIs are used.
- The JAR plus target/lib launches. Javadoc generates valid HTML; warnings and public contracts are reviewed.
- Missing/wrong host, port or database is rejected. The actual database is discovered or requested, never guessed.
- Credentials are absent from Git, logs, JAR and documentation. TLS validates certificate and hostname.
- Normal mode validates schema. Update requires the explicit flag and exact target. No paid Azure resource is automatically created.
- Real SQL Server identity, nullability, indexes, FKs and unique constraints are inspected. Two separate registrations do not fail on a nullable active-vehicle FK.

## Domain and transaction integration
- Cancelling each registration step leaves no user, vehicle or history. Final registration saves all components atomically.
- A user cannot read or modify another user's vehicle, service or problem. Active vehicle must be owned.
- Last-vehicle deletion is rejected. Two concurrent deletions cannot leave no vehicles. Active deletion switches to an owned replacement in the correct FK order.
- Shared catalog entries survive vehicle deletion. Used vehicle identity is protected after history/problems exist.
- A normal service contains at least one unique work item and a known nonnegative actual price. Initial-history NULL stays unknown; zero stays a known zero.
- Higher service mileage raises current mileage; a backdated lower value does not reduce it.
- An invalid problem at the end of service creation rolls back record, items, mileage and prior problem changes.
- A resolving service belongs to the same vehicle. Unknown or foreign IDs fail cleanly.
- Same request key and payload returns the existing ID. The same key with changed payload is a conflict.
- An uncertain commit is checked with the same key, not repeated blindly with a new key.
- Stale version/form input is rejected. A committed save followed by failed refresh is not labelled a failed save.
- Bulk deletion or update does not leave a stale managed graph that is subsequently reused incorrectly.

## Maintenance, diagnostics and prices
- The first reached distance/calendar threshold is DUE, including the exact boundary. plusMonths handles month-end and leap years.
- Missing history is UNKNOWN_HISTORY. Missing period is UNKNOWN_INTERVAL or condition/indicator status, not OK.
- Repairs and HV battery do not receive an invented replacement period. Checking brake fluid is not replacing it.
- An initial-only coolant period is not flattened into a recurring interval. Model-year and market exceptions remain under review.
- Latest maintenance is selected by execution date, mileage and ID, not latest INSERT.
- Estimate 53 becomes50,284 becomes280,285 becomes290. Actual53.47 remains53.47. NULL is not0.
- Oil package includes its filter. A disk-and-pad package cannot additionally count the same pads. Alternative DPF operations are not combined as one recommended bill.
- A real itemized invoice may still be entered faithfully. OTHER_ entries require an explanatory note.
- EV suggestions exclude combustion oil, timing belt and glow plugs. Scoped HV battery/charging works may exist with a NULL quote.
- Petrol, diesel, hybrid, drum-brake, unknown-gearbox and classic/exotic cases have sensible inclusion and exclusion behaviour.
- Repeated phrases do not increase diagnostic score. Boundaries and ties are deterministic. No match is a valid result that can be saved.
- Only applicable REPAIR candidates enter analysis. Score is not probability; main estimate is not the sum of all alternatives. Safety limitations are visible.

## GUI
- All 12 described flows work against the real database.
- Database work and hashing are off EDT. UI input is snapshotted before background work; completion updates UI on EDT.
- A late response for vehicle A is discarded after selecting B or logging out. Hidden panels become dirty without an unnecessary refresh storm.
- Pending writes disable duplicate submission. Cancel/X/Escape respect dirty and busy states.
- The last edited cell is committed before save; invalid content remains visible. Sorted row selection resolves the correct model ID.
- Large catalogs use bounded search rather than enormous combo boxes. Dependent selection is reset appropriately.
- Check 1366x768 and 100/125/150 percent scaling, keyboard navigation, tooltips, wrapping and unclipped action buttons.
- The fallback works without internet. The ordinary application performs no external vehicle/image/AI API calls.

## Real SQL Server seed, beyond a dry run
- All hashes and input rows are validated before write; corrupt hash, duplicate pair or overflow fails.
- Sample of8variants/403rules,122works and87diagnostic rules inserts and appears correctly.
- On an empty catalog, full code counts match30,366variants,122works and1,650,435rules. Existing manual/legacy rows are not deleted to force a count.
- A second identical seed creates no duplicate code/pair and preserves a manually entered price and interval.
- Interruption after a committed chunk can be resumed in an isolated target. Concurrent seed lock is detected.
- Driver bulk-copy and temporary-table behaviour is verified with the actual13.4driver; compare --plain-jdbc if necessary.
- No user table, actual price or historical problem snapshot is changed by reference import.
- Old DEMO diagnostic deactivation occurs only for exact recognised unmodified signatures; custom rules are not silently removed.
- The34referenced periods require the opt-in. The676review candidates are not automatically promoted.
- Reviewed interval-only import changes no price; existing period conflict requires explicit review. A plan valid for only part of a variant's range is not applied to the whole range.
- No TRUNCATE, disabled TLS/FKs or arbitrary hardcoded IDs. Measure duration and compute rather than promising them.

## Images and final deliverables
- Lookup creates DRAFT only. Representative year or matching model text alone is not approval.
- Exact generation and licence are reviewed. Manual Commons filename override works.
- Size/format/redirect/host/cache/backoff guards work. Missing licence/restrictions fail; a good existing hash avoids re-download.
- Author/source/licence/change credits are bundled as CSV/HTML. Images are not BLOBs.
- JAR rebuild precedes path import. Missing classpath bytes or incorrect hash is rejected; replacing a different existing path requires explicit override.
- Unreviewed groups retain fallback; actual approval counts are reported honestly.
- Final report, Javadoc, UML/ERD, explained wireframes, actual screenshots, dependencies/sources/licences and setup instructions are present.
- Git DAG/HEAD comes from real history. Commits are meaningful and not backdated. AI assistance is acknowledged as required by the course.
