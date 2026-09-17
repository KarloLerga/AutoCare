# Versions, provenance and primary sources - AF3
Checked 2026-09-16. Pinning is a reproducibility decision, not a claim that a real build ran in preparation. If a pinned artifact fails to resolve, inspect official release/Maven information and apply the smallest documented correction; never replace it with a fake/stub jar.

| Component | Pin | Use / official source |
|---|---|---|
| Java / Temurin | release 25, user's local 25.0.4.1 | Standard language, Swing, JDBC, PBKDF2; https://docs.oracle.com/en/java/javase/25/ |
| Maven | 3.9.16 | Build; https://maven.apache.org/ref/3.9.16/apache-maven/ |
| Jakarta Persistence | 3.2.0 | Mapping and EntityManager API; https://jakarta.ee/specifications/persistence/3.2/ |
| Hibernate ORM | 7.4.8.Final | JPA provider; runtime koristi mali built-in pool za single-user desktop; https://hibernate.org/orm/releases/7.4/ |
| Microsoft JDBC | 13.4.0.jre11 | SQL Server driver, jre11 artifact supports Java11+ including25; https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver17 |
| FlatLaf | 3.6.2 | Standard Swing look and feel; https://www.formdev.com/flatlaf/ |
| SLF4J JUL binding | 2.0.17 | Logs to JDK logging; https://www.slf4j.org/ |
| JUnit Jupiter | 5.13.4 | Test only; https://docs.junit.org/5.13.4/user-guide/ |
| compiler / surefire / failsafe | 3.14.1 / 3.5.4 / 3.5.4 | Maven compiler/unit/integration tests |
| jar / dependency / javadoc | 3.4.2 / 3.8.1 / 3.12.0 | Package + runtime lib directory + API docs; https://maven.apache.org/plugins/ |
| wrapper plugin | 3.3.4 | Generates official only-script wrapper locally; https://maven.apache.org/wrapper/ |
| Python | 3.11+ | Optional offline data/image tooling, NOT application runtime |
| Pillow | 12.3.0 | Image processing only; https://pillow.readthedocs.io/ |
| Graphviz | system development tool | Render included DOT sources, not runtime dependency; https://graphviz.org/ |

Maven resolves Hibernate transitive libraries and JUnit components. Generate `mvn dependency:tree` on the real workstation for the exact effective inventory; do not infer versions of transitive jars from an unexecuted build. No HikariCP dependency or MySQL connector remains in the active runtime database path.

## Database references
- Free offer configuration, limits, opt-in billing and connections preventing pause: https://learn.microsoft.com/en-us/azure/azure-sql/database/free-offer?view=azuresql
- JDBC properties, encryption, login/socket timeout: https://learn.microsoft.com/en-us/sql/connect/jdbc/setting-the-connection-properties?view=sql-server-ver17
- Connectivity troubleshooting: https://learn.microsoft.com/en-us/azure/azure-sql/database/troubleshoot-common-connectivity-issues?view=azuresql
- JDBC batch-insert bulk-copy support: https://learn.microsoft.com/en-us/sql/connect/jdbc/use-bulk-copy-api-batch-insert-operation?view=sql-server-ver17
- VS Code MSSQL: https://learn.microsoft.com/en-us/sql/tools/visual-studio-code-extensions/mssql/mssql-extension-visual-studio-code?view=sql-server-ver17

## Vehicle and price/schedule sources
- Original user-provided snapshot: https://github.com/gor3a/vehicle-makes-models ; reference upstream commit b4965631140fa82404359167621274601da69a5b. Raw bytes and hashes retained; do not claim the ZIP itself proves a signed commit. Dataset licence ODbL1.0; code MIT. Upstream attribution includes autoevolution.com. Dataset contains vehicle facts, not Croatian prices or complete fitment/OEM schedules.
- Rijeka workshop 2026 price list: https://www.ak-rijeka.hr/wp-content/uploads/Cjenik-autoservis-2026.pdf . 60 EUR gross labour hour is the model anchor, not a national average.
- Auto Kantoci 2026: https://www.autokantoci.hr/wp-content/uploads/2026/01/Cjenik_Auto_Kantoci_07_01_2026.pdf . Net hourly observations cannot be mixed directly with gross totals.
- Toyota Croatia model plans (tables viewed as rendered PDFs):
  https://www.toyotaadria.com/hr/pdf/cjenici_servis/2021/plan-servis-CHR-CRO-2021.pdf
  https://www.toyotaadria.com/hr/pdf/cjenici_servis/2021/plan-servis-Yaris-CRO-2021.pdf
  https://www.toyotaadria.com/hr/pdf/cjenici_servis/2021/plan-servis-RAV4-CRO-2021.pdf
  Only selected hybrid generations/operations admitted. Publication age and exact model scope retained. Initial coolant service is not rewritten as a recurring generic period.
- Tesla Model3 manual: https://www.tesla.com/ownersmanual/model3/en_eu/GUID-E95DAAD9-646E-4249-9930-B109ED7B1D91.html . Fluid check differs from replacement; tyres and salt-region operations have conditions.
- Tesla ModelY2020-2024: https://www.tesla.com/ownersmanual/2020_2024_modely/en_eu/GUID-E95DAAD9-646E-4249-9930-B109ED7B1D91.html . Broad catalog generation starts2019; candidate rows kept for review, not silently applied to the whole range.
- Kia Germany Sep2025 service table (three rendered pages inspected): https://eu-www.kia.com/content/dam/kwcms/kme/de/de/assets/contents/service/service-wartung/Kia_Wartungsintervalle-Fahrzeuge_Stand-2025-09.pdf . Market-specific potential matches are review candidates, not verified Croatian schedules.

`tools/reference-data/data/source_register.csv` and `source_register_af3.csv` hold the operation/source details. Parts prices, labour times and modelling coefficients not present in those public sources remain explicitly labelled model assumptions. Published schedules do not validate every generated price. No claimed measured model error or national survey.

## Image enrichment sources
- Candidate considered: https://github.com/trustcarinfo/carapi . No runtime dependency and no assumption its year matching proves the generation.
- Direct Wikidata access chosen for a small offline Python tool: https://www.wikidata.org/wiki/Wikidata:Data_access
- Commons imageinfo metadata: https://www.mediawiki.org/wiki/API:Imageinfo
- User-Agent policy: https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy
- General Commons reuse requirements: https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia
Specific image author/licence/source must come from each reviewed image record, not a generic statement that everything on Wikimedia is freely usable. No real photographs have been downloaded or approved during this delivery.

## Course / supplied sources
Original handoff, professor's review of the earlier concept, amended concept, architecture/UML PNGs, 12-page GUI PDF and OOP/NOOP archive are retained outside the Git project in source-materials/. Original terminology and requirements are traced in COURSE_ALIGNMENT_AND_DEFENSE.md. The database change, SQL import staging, private credential workflow and image review pipeline are AF3 design decisions, not claims that professor supplied this exact implementation.

## Local Codex working instructions
`AGENTS.md` is a local repository instruction file; official context: https://developers.openai.com/codex/guides/agents-md . The package does not call OpenAI services, require an API key or add a runtime AI dependency.
