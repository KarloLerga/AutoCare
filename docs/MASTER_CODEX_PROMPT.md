# MASTER IMPLEMENTATION / INTEGRATION PROMPT - AUTOCARE AF3 AZURE SQL

## Aktualna V2 implementacija

Ovaj AF3 prompt ostaje povijesni ulazni ugovor, ali zadnji V2 plan je autoritativan za trenutno
stanje izvornog koda. V2 zadrzava Java25/Maven/Swing/FlatLaf/JPA/Hibernate/Azure SQL, pet repository
sucelja i dijagnosticki tok bez runtime API-ja. Uklonjeni su servisni `ServiceRecord.requestKey`,
service-save idempotency/retry state, owner lockovi, DTO version flow, session epoch/ticket mehanizam,
service-history paging i `EstimateSelection`. `Problem.requestKey` i ostali diagnostics tipovi ostaju
zamrznuti. Odrzavanje koristi samo `NO_DATA`, `OK`, `SOON`, `DUE`, uz nullable WorkDefinition
default intervale. Za tocne aktualne potpise koristi `docs/TYPE_CATALOG.md`, a za stvarne rezultate
`docs/IMPLEMENTATION_STATUS.md` i `docs/VERIFICATION.md`.

You are the local implementer, integrator and debugger in Karlo Lerga's VS Code on Windows. Respond to the user in Croatian. This package contains concrete implementation files, not merely a proposed architecture. Work through the phases until the application and course deliverables work on the actual workstation. Do not stop at a plan, copied files or an impressive report. Fix real defects in the supplied implementation and record evidence. Never claim execution when you only inspected code.

## 0. Inputs and precedence
- TARGET_REPO: the actual local checkout of `https://github.com/KarloLerga/AutoCare`. It was empty when inspected for this delivery; do not assume it is still empty.
- PACKAGE_ROOT: this complete package, preferably outside TARGET_REPO.
- REFERENCE_SOURCE: PACKAGE_ROOT/project.
- PRIVATE_CONFIG: PACKAGE_ROOT/private/connection.local.json, OUTSIDE the repository. The user authorized these test credentials. Do not print or commit them; rotate before real use.
- COURSE_INPUTS: PACKAGE_ROOT/source-materials, original course archive and concepts. Read locally; do NOT commit or republish teaching materials.
- HISTORY: PACKAGE_ROOT/history, earlier AF1/AF2/MySQL deliverables preserved only as historical evidence. Never execute those against Azure SQL.

Precedence: latest user requirements -> mandatory course deliverables -> AF3 architecture and current source -> current data contracts -> historical conceptual material. Azure SQL Database / SQL Server is now final, overriding EVERY earlier MySQL mention, including the copied image-phase description. There is no MySQL runtime dependency in the current project.

Actual server: auto-care.database.windows.net, port1433. Actual DATABASE NAME was NOT supplied. Read-only `db-list` discovery exists; one accessible DB can be selected, several require user selection, no result requires an exact name from the portal. Never invent the name from the server/repo, create a paid replacement, change free-offer behaviour, or open all firewall IPs.

## 1. Read before changing code
Read START_HERE, project/README, docs/ARCHITECTURE_FREEZE_AF3.md (A-O), DATABASE_AND_JPA, TYPE_CATALOG, GUI_AND_WIREFRAMES, VERIFICATION, ACCEPTANCE, AZURE_SETUP, PROJECT_REPORT, COURSE_ALIGNMENT_AND_DEFENSE, DEPENDENCIES_AND_SOURCES. Then read actual Java sources relevant to the phase, tests and tools/reference-data/README_HR.md. Image README is for the last phase.

The original handoff requests small comprehensible Java classes and real Panjkota course principles. Original review asks for clear responsibilities, diagrams, transactions and reasoned patterns, not enterprise complexity. Inspect the actual relevant Strategy/Observer/MVC/SOLID examples at paths listed in course alignment. This implementation is an adaptation, not a claim to copy professor's exact TransactionRunner or SQL tooling.

## 2. Environment, Git and confidentiality
At F0 inspect current working directory, git status/branch, current remote, recent commits, installed Java/javac/Maven and VS Code Java runtime. Do not dump environment variables, connection objects, PRIVATE_CONFIG contents or authenticated remote URLs with tokens.

```powershell
Get-Location
git status --short --branch
git log -5 --oneline
Get-Command java,javac,mvn,git -ErrorAction SilentlyContinue
java -version
javac -version
mvn -version
```
An empty git log is expected for a genuinely new repository, not a reason to reset it. Preserve uncommitted user changes. Do not use reset --hard, clean -fd, force-push, backdating, fabricated commits or delete/re-add cycles to disguise reference-code provenance. Use the installed authenticated Git CLI; do not ask for a PAT in chat. Do not invent name/email in Git config; ask only if genuinely missing.

Copy/integrate ONLY project/ into the target. Never add PACKAGE_ROOT itself, private/, history/, source-materials/, original ZIPs, JDK/Maven binaries, .tools or generated test caches. If the project is not empty, diff and integrate selectively. Do not replace working code blindly.

Meaningful phase: actual change -> compile/test -> fix -> diff --check -> staged review + scripts/check-secrets.ps1 -> coherent commit -> record SHA/tests. Push normal commits to the user's specified repo only through the already-authorized CLI and branch workflow. No push to an unrelated remote. Missing write authorization blocks push, not local implementation/testing. AI assistance is disclosed according to course rules; Git history is not evidence of unaided authorship.

## 3. Technology and simplicity constraints
Java25, no preview, Maven, standard Swing + FlatLaf Light, Jakarta Persistence EntityManager/Factory, Hibernate provider, Microsoft SQL Server JDBC. Plain constructor injection in Main. No Spring/SpringData, Lombok, DI framework, MapStruct, JavaFX, web frontend, runtime AI/vehicle API, reactive framework, generic BaseRepository or custom ORM.

Pins: Maven3.9.16; JakartaPersistence3.2.0; Hibernate-core and hikaricp7.4.8.Final; mssql-jdbc13.4.0.jre11; FlatLaf3.6.2; slf4j-jdk142.0.17; JUnitJupiter5.13.4. Plugins in pom.xml. jre11 is the driver artifact's minimum baseline, NOT a request to downgrade Java25. The user's `java`, `javac`, Maven JAVA_HOME and VS Code JDK must all be25. Do not change release to21 because preparation tests used21.

Generate official Maven wrapper (only-script) using the pinned wrapper plugin after Maven is available. bootstrap-maven.ps1 downloads official Maven ZIP + verifies SHA512. Review before execution. Correct a genuinely unavailable/incompatible pin only after checking the official source; update dependency documentation and regression test. Never insert API stubs/fake Hibernate to make a build pass. Temporary preparation stubs are not part of this delivery and are not permitted for runtime/testing claims.

## 4. Freeze of responsibilities and persistent model
Exactly nine persistent entities: User, Vehicle, VehicleVariant, ServiceRecord, ServiceItem, WorkDefinition, VehicleWorkRule, Problem, DiagnosticRule. One new enum/column `schedule_kind` distinguishes FIXED, CONDITION_BASED, VEHICLE_INDICATOR, UNKNOWN. No price-component, image, transient-result, due-state or event tables.

Packages: app composition/config, domain, model scalar Data classes, repository interfaces, persistence JPA implementations, service, strategy, controller, view/components, event, tools CLI. View knows neither SQL nor repository nor service implementation. Controller snapshots input and launches a use-case; service controls transaction and business rules; repository only queries/persists. Tools are developer infrastructure and never invoked by UI events.

Use JPA field access and protected no-arg constructors, encapsulated fields and meaningful state-changing methods. Only ServiceRecord.items is a required bidirectional collection, mappedBy serviceRecord and cascade ALL/orphanRemoval. All shared catalog references have NO cascade remove. No inverse collections merely for symmetry. No entity/proxy goes into JTable after closing EM.

User.activeVehicle is nullable ManyToOne WITHOUT a UNIQUE SQL constraint. Ownership makes the application relation more constrained; SQL Server unique+nullable semantics must not block a second new registration. Business rules ensure active vehicle belongs to that user and a completed user has >=1 vehicle. Schema intermediate NULL is permitted only inside coherent onboarding/deletion choreography. Ordinary long @Version maps BIGINT, not SQL rowversion.

ServiceItem actualPrice is nullable ONLY for optional imported/onboarding history. Normal new service requires a user-entered nonnegative known value. Unknown is not0. CostSummary holds known sum and unknown item count. Estimates and actual prices never substitute for each other. Actual cents are retained; only estimates round to nearest10 HALF_UP.

No implicit default maintenance intervals on WorkDefinition. No auto suggestion based on a global work price that could give oil service to an EV. VWR presence is scoped catalog coverage; absence can mean unknown, not impossible. OTHER_MAINTENANCE / OTHER_REPAIR are history escape hatches with a mandatory descriptive note, not automatic generic recommendations.

## 5. SQL Server / schema contract
Use dbo schema, SQLServerDialect, nationalized text, JDBC TLS encrypt=true/trustServerCertificate=false. Existing DB must exist before Hibernate. No MySQL connector, InnoDB, AUTO_INCREMENT, ON DUPLICATE KEY, SHOW CREATE TABLE, foreign_key_checks, mysql SSL flags or3306.

SqlSettings is shared pure config for JPA and JDBC tools. User/password are Properties, not concatenated in URL. Host/name validated, system DBs rejected except read-only db-list against master. GUI always validates schema. update is allowed ONLY for `schema-update --confirm-development-schema` with exact AUTOCARE_SCHEMA_TARGET=actual DB. AUTOCARE_SEED_TARGET independently confirms seed/image/review writes. Test namespace must be distinct and name end_test.

Audit real Hibernate output on SQL Server: identity PK, optional active FK, VARCHAR/NVARCHAR lengths, enum mapping, BIT, DECIMAL(9,2)/matchpercent(5,2), DATE/DATETIME2, FK/index/UNIQUE pair constraints. Inspect sys.tables/sys.columns/sys.indexes or scripted DDL in MSSQL. validate alone does not prove every intended unique/check/index exists. If existing data predates schedule_kind, make an explicit reviewed backfill before NOT NULL; do not drop tables or lose historical rows. Fresh empty repo/DB is the expected baseline, not justification to destroy existing data.

Hikari max2/minIdle0/keepalive0 with idle cleanup. No continuous polling to keep free database warm. Do not relax TLS to fix cold-start. Initial connection/read retries may be bounded/backed off if needed; no automatic write retry without idempotency.

## 6. Use-cases and transactions
JpaTransactionRunner opens one new EM and RESOURCE_LOCAL transaction per callback and creates all five repository implementations with that same EM. Repository does NOT begin/commit/close. No ThreadLocal EM/global session; EMF one application lifecycle instance. No nested public service call opening a second transaction mid-use-case.

An authenticated write locks the owner User row first (PESSIMISTIC_WRITE) to protect cross-vehicle last-car invariant. @Version/expectedVersion rejects stale forms. Password hashing and UI waits never occur while holding DB lock.

Registration: keep account, vehicle and optional history as memory drafts. On final confirmation persist user -> vehicle -> active link -> historical records/items in ONE transaction. Cancel/back/window close must not persist a half user. Changing selected vehicle cannot silently carry inapplicable draft history.

Create service: validate, lock/verify owner, lookup same requestKey, validate selected own vehicle and work inputs, create record/items, max(currentMileage,newMileage), resolve selected own OPEN problems, commit. At least one item, unique work per record. A service can resolve0..N problems; one problem references0..1 resolving service; no ManyToMany. The actual repair need not equal the original guessed candidate.

Delete vehicle: cannot delete last; active deletion requires a selected alternative owned vehicle. Switch active and delete dependent problems/items/records/vehicle in correct FK order, one transaction. Shared definitions and variants stay. Identity of used vehicle is immutable after any service/problem; mileage only goes up. Backdated service does not reduce currentMileage.

ServiceRecord/Problem requestKey UUID makes an identical uncertain submit verifiable. Same key/different payload is a conflict, not success. Communication error during commit can mean unknown outcome. Keep key/freeze draft and resolve readback; never generate new key blindly. Commit success followed by view refresh failure is 'Saved, refresh failed', not 'save failed'. Rollback/close errors must not erase original cause or reverse reported successful commit.

## 7. Algorithms and query/fetch behaviour
Maintenance latest service by date DESC,mileage DESC,id DESC; not latest inserted row. FIXED: due at first reached distance or calendar plusMonths threshold, inclusive boundary. SOON uses min(3000,ceil20%km interval) or min(30,ceil20%actual calendar-day length). UNKNOWN_INTERVAL/UNKNOWN_HISTORY visible; CONDITION_BASED and VEHICLE_INDICATOR not fabricated deadlines. First vs repeated OEM interval cannot be modelled by one repeating integer: keep source exception, do not silently flatten.

DiagnosticStrategy consumes scalar RuleData, no JPA. Normalize lowercase/diacritics/whitespace with token boundaries; one rule at most once; duplicate normalized phrase for candidate does not increase count. Weighted coverage =100*matchedWeight/allActiveCandidateWeight. Deterministic rounding/tie order. Only scoped REPAIR candidate rules for active variant. Percent is text-rule coverage, NOT probability. Main estimate is first candidate, not sum of alternative diagnoses. No match can still save description. Source safety limitations visible; no assurance that a vehicle is safe to drive or DIY HV repair instructions.

Dashboard targeted sums/counts, no whole graph load. Catalog search is bounded and owner/history queries scoped. Service paging first fetches IDs then items/works for that page; do not collection-fetch-join paginate. No N+1 fix by making all EAGER. Input parameters bound, no SQL text concatenation. Never load1.65million VWRs into GUI: read one active variant's relevant rows.

## 8. GUI completion contract
Standard Swing components with FlatLightLaf.setup, light readable colours, consistent spacing. No custom painting or theme framework beyond LAF. One frame, auth/main shell, sidebar Dashboard/Vozila/Odrzavanje/Servisi/Problemi/Profil; active vehicle changed only on Vozila. No separate Costs module.

Appropriate controls: spinner numeric year/km, dependent make/model combo, searchable variant/work JTable for large option sets, strict formatted date, text area for descriptions, checkbox table for resolved problems, decimal editor for actual cost. Standard dialogs are acceptable. Escape/X/Cancel/back obey dirty/busy logic. Long text wraps/tooltips; status always includes text, not only colour. Check1366x768,100/125/150%DPI, keyboard focus/navigation.

NewService has one table for category/work/actual paid; no DB write until save. stopCellEditing before input snapshot, reject invalid cell. Sorted selection converts view to model index. OTHER_ needs note. Estimate fields never auto-fill actual. WorkPicker displays 'Procijenjena ukupna cijena', no foreign-language leftovers. NULL estimate='Nema procjene'; known number approximately N EUR, nearest10. Existing account totals/history use exact decimals.

Estimate calculator is temporary. Reject overlapping package contents/alternative repair totals through EstimateSelection. Do not prohibit faithful manual transcription of a real invoice with itemized lines. Original ENGINE_OIL/OIL_FILTER history is not rewritten as OIL_SERVICE or reset simply because new work definitions exist. Any history alias needs explicit semantics and tests.

All DB I/O and password hashing off EDT using SwingWorker. Snapshot UI inputs before background work; done returns to EDT. Session epoch + request ticket reject stale completion after logout/vehicle switch. Emit AppEvents after commit, mark hidden views dirty and refresh visible one; no global storm of parallel reads. Image component reads only classpath JPEG/fallback, caches bounded/aspect-correct results, no network.

## 9. Actual data, completeness and default safety
Prebuilt `tools/reference-data/data` is authoritative input, no new LLM call required. Source ZIP has30390engine rows; normalized30366variants,122workdefs,1282916numeric estimates,1650435seed rules including applicable NULL-price rows. Full audit3704652pairs and1179688conditional combinations kept separately. 3500variants have no numerical price. These are model outputs, not verified individual market facts. Do not label them APPROVED, official, statistically average or VIN-specific to make completeness look better.

Parts+labour are ONE gross estimate, rounded10; model components exist only in offline audit/config. They do not require new DB tables/UI fields. Operation scope matters (one injector, both wheels of one axle, package with filter etc.). 122 is a broad practical catalog, not every imaginable repair. Missing operation can be added with work code/provenance later, and OTHER_ with note records a real invoice now.

EV-specific battery/charging/inverter/drive-unit definitions exist. No engine oil/glow plugs/timing belt for a BEV. Do not infer belt, DPF, flywheel, refrigerant or transmission maintenance spec solely from model text/year. CONDITIONAL remains outside default seed. NULL price is often the correct result for HV pack/exotic/classic without quote; do not substitute zero or made-up average.

34 recurring source-referenced schedule rows are opt-in only; they're narrow model/generation scopes, not general brand defaults. 676review candidates (673Kia different market +3ModelY generation-range mismatch) are NOT active verified plans. First/repeat coolant, tyre rotation, salt-region operations remain documented exceptions. Condition-based wear and repair have no fake universal replacement interval.

Java seed pipeline validates SHA/content before DB, obtains session sp_getapplock, uses #temporary staging and prepared JDBC batches1000, set-based INSERT NOT EXISTS/update fills, commits each chunk. It does NOT use MERGE, TRUNCATE, disabled FKs or persist every row into a huge JPA context. The same JDBC driver as the app is used, optional driver bulk-copy optimization, --plain-jdbc fallback. Test real #temp/bulk-copy metadata behaviour with actual driver; adjust staging/disable optimization minimally if needed, never remove constraints.

Repeating seed preserves known user prices/intervals, existing codes/identities and all user tables. Existing incompatible work-code category fails. Recognized unmodified old DEMO diagnostic rules may be disabled before new rules, but modified/custom rules need review. No silent mixing/boosting of diagnostic denominators. Interrupted seed leaves earlier committed chunks; rerun resumes by stable code/pair.

Small reviewed intervals use `import-intervals reviewed.csv [--apply]`, max500, whole-variant scope confirmed, reviewer/date/source mandatory; no price changes. Existing conflict fails unless --replace-interval-only is explicitly chosen. Do not use old full-row replace to alter only a period. JSON/CSV lookup matching not sufficient to mark human source validation complete.

## 10. Staged image enrichment - execute near the end
No photo is required for a functional app. Current fallback and6697group plans exist, no real photos downloaded or approved in preparation. Direct Wikidata/Commons APIs chosen as small developer-only tool, not carapi server/runtime dependency.

Plan -> bounded lookup/cache -> DRAFT candidate mapping -> actual generation + licence review -> approved download/normalization -> packaged credits CSV/HTML -> rebuild JAR -> Java import-images with hash/resource checks. Do not auto-approve matching model text or representative year. User/manual override can choose exact Commons File. Retain ambiguous/no image fallback. Do not copy unlicensed thumbnails or add externalURLs as imagePath.

Use tools/images/README and actual CLI --help. Respect contact User-Agent/rate limits; small batches20 not huge crawler, resume viaoffset/cache. Supported input types/size caps/allowlisted HTTPS redirects. Output <=960x600 JPEG with aspect ratio. Store attribution/source/licence/changes/SHA in resources, never DB BLOB. Preserve SA obligations when applicable; unsupported licence goes to review report. Failed fetch does not authorize substituting unrelated image. Rebuild before imagePath DB import so local resources actually exist in JAR. Never claim images done when lookup only produced drafts.

## 11. Developer setup and bounded automation
Use scripts/Complete-Setup.ps1 with external ConfigPath. It checks JDK25/Maven, generates real wrapper, builds/tests, discovers existing DB, verifies connection, saves confirmed name ONLY in privateJSON, optionally explicit schema-update, seed sample by default, validates and generates Javadoc. Windows script is not pre-executed; fix actual PowerShell5.1/7/native command issues if present.

```powershell
.\scripts\Complete-Setup.ps1 -ConfigPath 'ACTUAL_PRIVATE_PATH' -InitializeSchema
# After real functional validation, not before:
.\scripts\Complete-Setup.ps1 -ConfigPath 'ACTUAL_PRIVATE_PATH' -FullData
# Source-referenced intervals require deliberate opt-in:
.\scripts\Complete-Setup.ps1 -ConfigPath 'ACTUAL_PRIVATE_PATH' -FullData -WithReferencedIntervals
```
No new Azure resource, SKU resize, disabling overage pause, bypassing firewall, auto-installing drivers with privileges or silently changing global execution policy. Existing permission/network/name may require one user action. If blocked, ask only for missing detail and continue offline work. Do not wait for full price/image coverage before proving main app.

## 12. Phases and commits (actual increments, not theatrical history)
Every phase includes baseline/diff, real commands/tests, fixes and updated IMPLEMENTATION_STATUS. A reference file can be adapted, but no placeholder return-success or UnsupportedOperationException is allowed in final advertised features.

### F0 - inspect / status
Resolve paths, Git state, JDK/Maven, private config availability (no printing), actual DB name/discovery/firewall. Read required docs. Record environment in docs/evidence/local-environment.md; PASSWORD REDACTED/never present. Do not invent a commit without change.

### F1 - reproducible build/config
Integrate pom, gitignore, .vscode examples and setup scripts. Official wrapper generation; `clean verify`, actual dependency tree. If complete reference copied as initial real import, commit its honest origin; do not split later by deleting/readding to fabricate chronology. If integrating incrementally, each stage must remain coherent and buildable.
Commit example: chore: establish Java 25 Maven and SQL Server configuration.

### F2 - domain and algorithms
Integrate9entities, Data results/drafts, enums, calculators, Strategy, errors/hash. Unit tests for moneyNULL, mileage,date boundary,period kinds,score,duplicates,EVfilters/overlap. Fix constructor/domain constraints as needed.
Commit example: feat: add AutoCare domain and deterministic calculations.

### F3 - persistence/services and sample
Integrate the JPA runner, five repositories, services and developer CLI. Run sql-check, explicit-target schema update, real provider validation and schema inspection. Import the sample. Test registration of at least two users, a failing service rollback, ownership, stale version, request-key replay, deleting the last/active vehicle and SQL date/enum mapping.

The sqlserver-it profile requires a distinct test database. Without one, mark the integration profile BLOCKED. Never redirect destructive scenarios to the normal database or claim that H2 proves SQL Server compatibility. A code commit and a later integration-evidence commit may be separate when infrastructure is temporarily unavailable.

### F4 - login/onboarding and navigation
Connect the real views/controllers and SwingWorker. Registration must save the first vehicle and optional history together. Cancelling before completion leaves no rows. Changing the selected variant must not silently transfer incompatible history. The active vehicle survives logout/login. Do not leave mock database results as the final implementation. Test dirty state and pending requests.
Commit example: `feat: connect authentication and vehicle onboarding`.

### F5 - vehicles and services
Complete allowed-field editing, activation, deletion, service-history paging, draft editing, actual costs and problem resolution. Test out-of-order worker completions, committing the last edited cell, sorted-table selection and cancellation. Correct actual layout or wording defects with standard Swing controls.
Commit example: `feat: complete vehicle management and atomic service entry`.

### F6 - maintenance/dashboard/problems/profile
Verify fixed/unknown/condition statuses, scoped work lists, exact actual sums, rounded estimates, incompatible-work exclusion, Strategy results and saved snapshots, profile authentication and logout. Add any missing safety notice without turning the result into a claim about safe driving.
Commit example: `feat: finish maintenance diagnostics and profile flows`.

### F7 - full data integration
Run the offline data tests and validation first. Then run actual sample and full imports with explicit model-estimate acknowledgement. Measure duration, counts and SQL behaviour. Repeat the same import: no duplicate codes/pairs. Check a known manually entered price/interval remains unchanged. Test interruption after a committed chunk and safe restart only on an isolated test target.

Compare the manifest's code subsets rather than deleting legacy rows to force total counts to match. Source-referenced intervals remain opt-in; do not mass-approve the 676 review candidates. Update data evidence and attribution.
Commit example: `data: integrate scoped vehicle work estimates and reviewed schedules`.

### F8 - vehicle images
Run this only after core application/database flows work. Perform bounded candidate lookup, actual generation/licence review and approved local download. Rebuild the JAR, then import image paths. A representative set of checked images is useful; unresolved groups retain the fallback. Do not block the usable application because every one of the 6,697 groups lacks a verified photo. Report the exact approved/candidate/fallback counts.
Commit example: `feat: add reviewed local vehicle image enrichment`.

### F9 - documentation, real tests and packaging
Generate API documentation. Align UML, ERD, ScheduleKind and FK descriptions with actual code and database schema. Include descriptions of all wireframes, current Windows screenshots, course-pattern explanations, dependencies, source/licence inventory, setup instructions and a 35-minute demonstration route. Export the actual Git DAG and current HEAD; no fabricated graph.

Run the acceptance matrix and actual compile/test/package/Javadoc commands. Keep target/lib alongside the JAR for local distribution; do not commit downloaded dependency binaries. Check staged content, generated documentation and release archives for credentials. Finish with one clear launch command and a status table of executed tests, remaining SQL/GUI/image blockers and commits.
Commit example: `docs: finalize verified AutoCare project and course deliverables`.

## 13. Required evidence / exit criteria
Run actual `.\mvnw.cmd clean verify` and `.\mvnw.cmd javadoc:javadoc` with Java 25 and real dependencies. A unit-test pass does not prove SQL behaviour. Run the SQL Server integration profile when an isolated test database is available; otherwise mark it BLOCKED. Record seed hashes, first/second import counts, FK/index inspection and real GUI interactions. Never include environment secrets.

Save actual manual GUI test notes and screenshots at 100/125/150 percent scaling. Do not count preparation-only compilation with API substitutes as a real Maven build.

Maintain docs/IMPLEMENTATION_STATUS.md: phase, status (PASS/BLOCKED/NOT_RUN), command/evidence, commit, blocker and next action. Preserve previous notes. TODO is allowed for genuinely blocked external work, not for a feature advertised as working. Subsequent runs resume with the same database and idempotency keys.

Final response in Croatian: what actually works, exact launch/import commands, test results, estimate/schedule limits, approved photos versus fallbacks and any required user action. No invented guarantees, no request to redesign the project from scratch and no promise of unexecuted background work. The goal is a clean, working project the student can explain, not the largest number of abstractions or files.
