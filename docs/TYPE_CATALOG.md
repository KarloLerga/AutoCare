# Actual Java type and API catalogue - AF3

86 production source files. Paths refer to project root. Public/protected signatures below are extracted from actual source, not an invented implementation plan. Package responsibilities/dependencies and entity fields are explained in ARCHITECTURE_FREEZE_AF3.md and DATABASE_AND_JPA.md.

## hr.unizd.autocare.app.DatabaseConfig

File: `src/main/java/hr/unizd/autocare/app/DatabaseConfig.java`

Declared types: DatabaseConfig

```java
public static EntityManagerFactory open(String schemaAction, boolean test) {
```
## hr.unizd.autocare.app.Main

File: `src/main/java/hr/unizd/autocare/app/Main.java`

Declared types: Main

```java
public static void main(String[] args) {
protected EntityManagerFactory doInBackground() {
protected void done() {
```

## hr.unizd.autocare.app.Session

File: `src/main/java/hr/unizd/autocare/app/Session.java`

Declared types: Session

```java
public void login(long owner) {
public void logout() {
public long owner() {
public long epoch() {
public VehicleRow active() {
public void setActive(VehicleRow value) {
public boolean isWriting() {
public void setWriting(boolean writing) {
```

## hr.unizd.autocare.app.SqlSettings

File: `src/main/java/hr/unizd/autocare/app/SqlSettings.java`

Declared types: SqlSettings

```java
public static SqlSettings environment(boolean test) {
public static SqlSettings discovery() {
public static SqlSettings from(Map<String,String> env, boolean test, boolean discovery) {
public String database() { return database; }
public String host() { return host; }
public String user() { return user; }
public String password() { return password; }
public String url() {
public Connection connect(boolean importer) throws SQLException {
public void requireSchemaConsent(Map<String,String> env) {
```

## hr.unizd.autocare.controller.AuthController

File: `src/main/java/hr/unizd/autocare/controller/AuthController.java`

Declared types: AuthController

```java
public AuthController(MainFrame frame, AuthService auth, CatalogService catalog, Session session, LongConsumer entered) {
```

## hr.unizd.autocare.controller.MainController

File: `src/main/java/hr/unizd/autocare/controller/MainController.java`

Declared types: MainController

```java
public MainController(MainFrame frame, Session session, AuthService auth, CatalogService catalog, VehicleService vehicleService, ServiceRecordService serviceRecords, MaintenanceService maintenanceService, ProblemService problemService, DashboardService dashboard, AppEvents events, Runnable shutdown) {
public void windowClosing(WindowEvent e) {
```

## hr.unizd.autocare.controller.MaintenanceController

File: `src/main/java/hr/unizd/autocare/controller/MaintenanceController.java`

Declared types: MaintenanceController

```java
public MaintenanceController(MainFrame frame, MaintenanceService service, Session session) {
public void load() {
```

## hr.unizd.autocare.controller.ProblemsController

File: `src/main/java/hr/unizd/autocare/controller/ProblemsController.java`

Declared types: ProblemsController, AnalysisFlow

```java
public ProblemsController(MainFrame frame, ProblemService service, Session session, AppEvents events) {
public void load() {
public void insertUpdate(DocumentEvent e) {
public void removeUpdate(DocumentEvent e) {
public void changedUpdate(DocumentEvent e) {
```

## hr.unizd.autocare.controller.ProfileController

File: `src/main/java/hr/unizd/autocare/controller/ProfileController.java`

Declared types: ProfileController

```java
public ProfileController(MainFrame frame, AuthService service, Session session, AppEvents events, Runnable logout) {
public void load() {
public boolean canLeave() {
public void clear() {
```

## hr.unizd.autocare.controller.ServiceEditorController

File: `src/main/java/hr/unizd/autocare/controller/ServiceEditorController.java`

Declared types: ServiceEditorController

```java
public ServiceEditorController(ServiceEditorDialog view, List<WorkRow> works, Session session, Consumer<ServiceInput> submit) {
```

## hr.unizd.autocare.controller.ServicesController

File: `src/main/java/hr/unizd/autocare/controller/ServicesController.java`

Declared types: ServicesController, FrozenForm, EditorData

```java
public ServicesController(MainFrame frame, ServiceRecordService service, CatalogService catalog, ProblemService problems, Session session, AppEvents events) {
public void load() {
```

## hr.unizd.autocare.controller.UiTasks

File: `src/main/java/hr/unizd/autocare/controller/UiTasks.java`

Declared types: UiTasks

```java
public UiTasks(Session session) {
public void invalidate() {
public <T>void read(Component parent, Callable<T> work, Consumer<T> success) {
public <T>void write(Component parent, Callable<T> work, Consumer<T> success) {
public <T>void run(Component parent, boolean write, Callable<T> work, Consumer<T> success, Consumer<Throwable> failure) {
public static boolean uncertain(Throwable error) {
```

## hr.unizd.autocare.controller.VehicleFormController

File: `src/main/java/hr/unizd/autocare/controller/VehicleFormController.java`

Declared types: VehicleFormController

```java
public VehicleFormController(VehicleForm view, CatalogService service, Session session) {
public void loadMakes() {
```

## hr.unizd.autocare.controller.VehiclesController

File: `src/main/java/hr/unizd/autocare/controller/VehiclesController.java`

Declared types: VehiclesController

```java
public VehiclesController(MainFrame frame, VehicleService service, CatalogService catalog, Session session, AppEvents events) {
public void load() {
```

## hr.unizd.autocare.domain.Checks

File: `src/main/java/hr/unizd/autocare/domain/Checks.java`

Declared types: Checks

```java
public static String text(String value, int max, String label) {
public static String optional(String value, int max, String label) {
public static String email(String value) {
public static int mileage(int value) {
public static BigDecimal money(BigDecimal value, boolean nullable) {
public static void password(char[] value) {
```

## hr.unizd.autocare.domain.CostSummary

File: `src/main/java/hr/unizd/autocare/domain/CostSummary.java`

Declared types: CostSummary

```java
public CostSummary(BigDecimal knownTotal, long unknownCount) {
public static CostSummary of(Collection<BigDecimal> prices) {
public BigDecimal getKnownTotal() {
public long getUnknownCount() {
```

## hr.unizd.autocare.domain.DiagnosticRule

File: `src/main/java/hr/unizd/autocare/domain/DiagnosticRule.java`

Declared types: DiagnosticRule

```java
protected DiagnosticRule() {
public DiagnosticRule(String code, WorkDefinition candidate, String phrase, int weight) {
public Long getId() {
public String getCode() {
public WorkDefinition getCandidate() {
public String getPhrase() {
public int getWeight() {
public boolean getActive() {
```

## hr.unizd.autocare.domain.EstimateSelection

File: `src/main/java/hr/unizd/autocare/domain/EstimateSelection.java`

Declared types: EstimateSelection

```java
public static Set<String> conflicts(Set<String> selectedCodes, String candidateCode) {
```

## hr.unizd.autocare.domain.MaintenanceCalculator

File: `src/main/java/hr/unizd/autocare/domain/MaintenanceCalculator.java`

Declared types: MaintenanceCalculator

```java
public MaintenanceStatus calculate(ScheduleKind kind, Integer km, Integer months, LocalDate date, Integer mileage, int current, LocalDate today) {
public MaintenanceStatus calculate(Integer intervalKm, Integer intervalMonths, LocalDate lastDate, Integer lastMileage, int currentMileage, LocalDate today) {
```

## hr.unizd.autocare.domain.MaintenanceStatus

File: `src/main/java/hr/unizd/autocare/domain/MaintenanceStatus.java`

Declared types: MaintenanceStatus

```java
```

## hr.unizd.autocare.domain.Problem

File: `src/main/java/hr/unizd/autocare/domain/Problem.java`

Declared types: Problem

```java
protected Problem() {
public Problem(Vehicle vehicle, String key, String description, LocalDateTime created, WorkDefinition suggestion, BigDecimal score, BigDecimal price, String estimateNote) {
public void resolve(ServiceRecord record) {
public Long getId() {
public long getVersion() {
public Vehicle getVehicle() {
public String getRequestKey() {
public String getDescription() {
public ProblemStatus getStatus() {
public LocalDateTime getCreatedAt() {
public WorkDefinition getSuggestedRepair() {
public BigDecimal getMatchPercent() {
public BigDecimal getEstimatedCost() {
public String getEstimateNote() {
public ServiceRecord getResolvedByService() {
```

## hr.unizd.autocare.domain.ProblemStatus

File: `src/main/java/hr/unizd/autocare/domain/ProblemStatus.java`

Declared types: ProblemStatus

```java
```

## hr.unizd.autocare.domain.ScheduleKind

File: `src/main/java/hr/unizd/autocare/domain/ScheduleKind.java`

Declared types: ScheduleKind

```java
```

## hr.unizd.autocare.domain.ServiceItem

File: `src/main/java/hr/unizd/autocare/domain/ServiceItem.java`

Declared types: ServiceItem

```java
protected ServiceItem() {
public Long getId() {
public ServiceRecord getServiceRecord() {
public WorkDefinition getWork() {
public BigDecimal getActualPrice() {
```

## hr.unizd.autocare.domain.ServiceRecord

File: `src/main/java/hr/unizd/autocare/domain/ServiceRecord.java`

Declared types: ServiceRecord

```java
protected ServiceRecord() {
public ServiceRecord(Vehicle vehicle, String key, LocalDate date, int mileage, String note) {
public void addItem(WorkDefinition work, BigDecimal actualPrice) {
public CostSummary total() {
public Long getId() {
public Vehicle getVehicle() {
public String getRequestKey() {
public LocalDate getDate() {
public int getMileage() {
public String getNote() {
public List<ServiceItem> getItems() {
```

## hr.unizd.autocare.domain.User

File: `src/main/java/hr/unizd/autocare/domain/User.java`

Declared types: User

```java
protected User() {
public User(String name, String email, String hash) {
public void activate(Vehicle vehicle) {
public void changeProfile(String name, String email) {
public void changePasswordHash(String hash) {
public Long getId() {
public long getVersion() {
public String getName() {
public String getEmail() {
public String getPasswordHash() {
public Vehicle getActiveVehicle() {
```

## hr.unizd.autocare.domain.Vehicle

File: `src/main/java/hr/unizd/autocare/domain/Vehicle.java`

Declared types: Vehicle

```java
protected Vehicle() {
public Vehicle(User owner, VehicleVariant variant, int year, int mileage) {
public void changeIdentity(VehicleVariant variant, int year) {
public void updateMileage(int mileage) {
public Long getId() {
public long getVersion() {
public User getOwner() {
public VehicleVariant getVariant() {
public int getYear() {
public int getCurrentMileage() {
```

## hr.unizd.autocare.domain.VehicleVariant

File: `src/main/java/hr/unizd/autocare/domain/VehicleVariant.java`

Declared types: VehicleVariant

```java
protected VehicleVariant() {
public VehicleVariant(String code, String make, String model, String generation, String engineLabel, int from, Integer to, String fuel) {
public VehicleVariant(String code, String make, String model, String generation, String engineLabel, int from, Integer to, String body, String fuel, Integer power, String transmission, String imagePath) {
public boolean covers(int year) {
public Long getId() {
public String getCode() {
public String getMake() {
public String getModel() {
public String getGeneration() {
public String getEngineLabel() {
public String getBodyType() {
public String getFuelType() {
public Integer getPowerHp() {
public String getTransmission() {
public int getYearFrom() {
public Integer getYearTo() {
public String getImagePath() {
```

## hr.unizd.autocare.domain.VehicleWorkRule

File: `src/main/java/hr/unizd/autocare/domain/VehicleWorkRule.java`

Declared types: VehicleWorkRule

```java
protected VehicleWorkRule() {
public VehicleWorkRule(VehicleVariant variant, WorkDefinition work, Integer km, Integer months, BigDecimal price, String intervalSource, String estimateNote) {
public void revise(Integer km, Integer months, BigDecimal price, String source, String priceNote) {
public Long getId() {
public VehicleVariant getVariant() {
public WorkDefinition getWork() {
public ScheduleKind getScheduleKind() {
public void defineScheduleKind(ScheduleKind kind) {
public Integer getIntervalKm() {
public Integer getIntervalMonths() {
public BigDecimal getEstimatedPrice() {
public String getIntervalSource() {
public String getEstimateNote() {
```

## hr.unizd.autocare.domain.WorkCategory

File: `src/main/java/hr/unizd/autocare/domain/WorkCategory.java`

Declared types: WorkCategory

```java
```

## hr.unizd.autocare.domain.WorkDefinition

File: `src/main/java/hr/unizd/autocare/domain/WorkDefinition.java`

Declared types: WorkDefinition

```java
protected WorkDefinition() {
public WorkDefinition(String code, String name, WorkCategory category, BigDecimal price, String estimateNote) {
public Long getId() {
public String getCode() {
public String getName() {
public WorkCategory getCategory() {
public BigDecimal getDefaultEstimatedPrice() {
public String getEstimateNote() {
```

## hr.unizd.autocare.event.AppEvent

File: `src/main/java/hr/unizd/autocare/event/AppEvent.java`

Declared types: AppEvent

```java
```

## hr.unizd.autocare.event.AppEvents

File: `src/main/java/hr/unizd/autocare/event/AppEvents.java`

Declared types: AppEvents

```java
public void add(AppListener listener) {
public void remove(AppListener listener) {
public void publish(AppEvent event) {
public void clear() {
```

## hr.unizd.autocare.event.AppListener

File: `src/main/java/hr/unizd/autocare/event/AppListener.java`

Declared types: AppListener

```java
```

## hr.unizd.autocare.model.Data

File: `src/main/java/hr/unizd/autocare/model/Data.java`

Declared types: Data, Account, Credentials, VariantRow, VehicleRow, VehicleInput, WorkRow, ItemInput, ServiceInput, ServiceRow, ItemRow, ServiceDetail, ProblemRow, RuleData, DiagnosticResult, Analysis, MaintenanceRow, Dashboard

```java
public Account(long id, long version, String name, String email, Long activeVehicleId) {
public long getId() {
public long getVersion() {
public String getName() {
public String getEmail() {
public Long getActiveVehicleId() {
public Credentials(long id, long version, String hash) {
public long getId() {
public long getVersion() {
public String getHash() {
public VariantRow(long id, String code, String make, String model, String generation, String engine, String fuel, String transmission, Integer powerHp, int from, Integer to, String imagePath) {
public long getId() {
public String getCode() {
public String getMake() {
public String getModel() {
public String getGeneration() {
public String getEngine() {
public String getFuel() {
public String getTransmission() {
public Integer getPowerHp() {
public int getFrom() {
public Integer getTo() {
public String getImagePath() {
public VehicleRow(long id, long version, VariantRow variant, int year, int mileage, boolean active) {
public long getId() {
public long getVersion() {
public VariantRow getVariant() {
public int getYear() {
public int getMileage() {
public boolean getActive() {
public VehicleInput(long variantId, int year, int mileage) {
public long getVariantId() {
public int getYear() {
public int getMileage() {
public WorkRow(long id, String code, String name, WorkCategory category, BigDecimal price, String priceNote) {
public String getCode() { return code; }
public long getId() {
public String getName() {
public WorkCategory getCategory() {
public BigDecimal getPrice() {
public String getPriceNote() {
public ItemInput(long workId, BigDecimal actualPrice) {
public long getWorkId() {
public BigDecimal getActualPrice() {
public ServiceInput(String requestKey, LocalDate date, int mileage, String note, List<ItemInput> items, List<Long> resolvedProblemIds) {
public String getRequestKey() {
public LocalDate getDate() {
public int getMileage() {
public String getNote() {
public List<ItemInput> getItems() {
public List<Long> getResolvedProblemIds() {
public ServiceRow(long id, LocalDate date, int mileage, String names, CostSummary total, String note) {
public long getId() {
public LocalDate getDate() {
public int getMileage() {
public String getNames() {
public CostSummary getTotal() {
public String getNote() {
public ItemRow(String name, WorkCategory category, BigDecimal actualPrice) {
public String getName() {
public WorkCategory getCategory() {
public BigDecimal getActualPrice() {
public ServiceDetail(ServiceRow header, List<ItemRow> items, List<String> resolvedProblems) {
public ServiceRow getHeader() {
public List<ItemRow> getItems() {
public List<String> getResolvedProblems() {
public ProblemRow(long id, long version, String description, ProblemStatus status, LocalDateTime createdAt, String suggestion, BigDecimal score, BigDecimal price, String priceNote, Long resolvedServiceId) {
public long getId() {
public long getVersion() {
public String getDescription() {
public ProblemStatus getStatus() {
public LocalDateTime getCreatedAt() {
public String getSuggestion() {
public BigDecimal getScore() {
public BigDecimal getPrice() {
public String getPriceNote() {
public Long getResolvedServiceId() {
public RuleData(long candidateId, String candidateName, String phrase, int weight, BigDecimal price, String priceNote) {
public long getCandidateId() {
public String getCandidateName() {
public String getPhrase() {
public int getWeight() {
public BigDecimal getPrice() {
public String getPriceNote() {
public DiagnosticResult(long candidateId, String candidateName, BigDecimal score, int matchedWeight, BigDecimal price, String priceNote) {
public long getCandidateId() {
public String getCandidateName() {
public BigDecimal getScore() {
public int getMatchedWeight() {
public BigDecimal getPrice() {
public String getPriceNote() {
public Analysis(String description, List<DiagnosticResult> results) {
public String getDescription() {
public List<DiagnosticResult> getResults() {
public MaintenanceRow(long workId, String workCode, String name, LocalDate lastDate, Integer lastMileage, LocalDate nextDate, Integer nextMileage, MaintenanceStatus status, BigDecimal price, String intervalSource, String priceNote, Integer remainingKm, Long remainingDays) {
public String getWorkCode() { return workCode; }
public long getWorkId() {
public String getName() {
public LocalDate getLastDate() {
public Integer getLastMileage() {
public LocalDate getNextDate() {
public Integer getNextMileage() {
public Integer getRemainingKm() {
public Long getRemainingDays() {
public MaintenanceStatus getStatus() {
public BigDecimal getPrice() {
public String getIntervalSource() {
public String getPriceNote() {
public Dashboard(VehicleRow vehicle, CostSummary total, long openProblems, int due, int soon, int unknown, int covered) {
public VehicleRow getVehicle() {
public CostSummary getTotal() {
public long getOpenProblems() {
public int getDue() {
public int getSoon() {
public int getUnknown() {
public int getCovered() {
```

## hr.unizd.autocare.persistence.JpaCatalogRepository

File: `src/main/java/hr/unizd/autocare/persistence/JpaCatalogRepository.java`

Declared types: JpaCatalogRepository

```java
public JpaCatalogRepository(EntityManager em) {
public List<String> makes(int y) {
public List<String> models(int y, String make) {
public List<VehicleVariant> variants(int y, String make, String model, String search) {
public VehicleVariant variant(long id) {
public List<WorkDefinition> works(WorkCategory category) {
public WorkDefinition work(long id) {
public List<VehicleWorkRule> rules(long variant) {
public List<DiagnosticRule> diagnosticRules() {
```

## hr.unizd.autocare.persistence.JpaProblemRepository

File: `src/main/java/hr/unizd/autocare/persistence/JpaProblemRepository.java`

Declared types: JpaProblemRepository

```java
public JpaProblemRepository(EntityManager em) {
public void add(Problem p) {
public Problem requireOwned(long owner, long id) {
public List<Problem> list(long owner, long vehicle, ProblemStatus status) {
public Optional<Problem> byRequest(long owner, String key) {
public List<String> resolvedDescriptions(long owner, long service) {
public long openCount(long owner, long vehicle) {
public void deleteForVehicle(long vehicle) {
```

## hr.unizd.autocare.persistence.JpaServiceRecordRepository

File: `src/main/java/hr/unizd/autocare/persistence/JpaServiceRecordRepository.java`

Declared types: JpaServiceRecordRepository

```java
public JpaServiceRecordRepository(EntityManager em) {
public void add(ServiceRecord s) {
public List<ServiceRecord> page(long owner, long vehicle, int offset, int limit) {
public ServiceRecord requireOwned(long owner, long id) {
public Optional<ServiceRecord> byRequest(long owner, String key) {
public List<ServiceItem> historyItems(long owner, long vehicle) {
public CostSummary total(long owner, long vehicle) {
public void deleteForVehicle(long vehicle) {
```

## hr.unizd.autocare.persistence.JpaTransactionRunner

File: `src/main/java/hr/unizd/autocare/persistence/JpaTransactionRunner.java`

Declared types: JpaTransactionRunner

```java
public JpaTransactionRunner(EntityManagerFactory factory) {
public <T>T read(Function<Repositories, T> action) {
public <T>T write(Function<Repositories, T> action) {
```

## hr.unizd.autocare.persistence.JpaUserRepository

File: `src/main/java/hr/unizd/autocare/persistence/JpaUserRepository.java`

Declared types: JpaUserRepository

```java
public JpaUserRepository(EntityManager em) {
public Optional<User> byEmail(String email) {
public User require(long id) {
public User lock(long id) {
public void add(User u) {
```

## hr.unizd.autocare.persistence.JpaVehicleRepository

File: `src/main/java/hr/unizd/autocare/persistence/JpaVehicleRepository.java`

Declared types: JpaVehicleRepository

```java
public JpaVehicleRepository(EntityManager em) {
public Vehicle requireOwned(long owner, long id) {
public List<Vehicle> list(long owner) {
public void add(Vehicle v) {
public void delete(Vehicle v) {
public boolean hasHistory(long vehicle) {
```

## hr.unizd.autocare.repository.CatalogRepository

File: `src/main/java/hr/unizd/autocare/repository/CatalogRepository.java`

Declared types: CatalogRepository

```java
```

## hr.unizd.autocare.repository.ProblemRepository

File: `src/main/java/hr/unizd/autocare/repository/ProblemRepository.java`

Declared types: ProblemRepository

```java
```

## hr.unizd.autocare.repository.Repositories

File: `src/main/java/hr/unizd/autocare/repository/Repositories.java`

Declared types: Repositories

```java
public Repositories(UserRepository users, VehicleRepository vehicles, ServiceRecordRepository services, ProblemRepository problems, CatalogRepository catalog) {
public UserRepository users() {
public VehicleRepository vehicles() {
public ServiceRecordRepository services() {
public ProblemRepository problems() {
public CatalogRepository catalog() {
```

## hr.unizd.autocare.repository.ServiceRecordRepository

File: `src/main/java/hr/unizd/autocare/repository/ServiceRecordRepository.java`

Declared types: ServiceRecordRepository

```java
```

## hr.unizd.autocare.repository.UserRepository

File: `src/main/java/hr/unizd/autocare/repository/UserRepository.java`

Declared types: UserRepository

```java
```

## hr.unizd.autocare.repository.VehicleRepository

File: `src/main/java/hr/unizd/autocare/repository/VehicleRepository.java`

Declared types: VehicleRepository

```java
```

## hr.unizd.autocare.service.AppException

File: `src/main/java/hr/unizd/autocare/service/AppException.java`

Declared types: AppException, Kind

```java
public AppException(Kind kind, String message) {
public AppException(Kind kind, String message, Throwable cause) {
public Kind getKind() {
public static AppException validation(String text) {
public static AppException conflict(String text) {
```

## hr.unizd.autocare.service.AuthService

File: `src/main/java/hr/unizd/autocare/service/AuthService.java`

Declared types: AuthService

```java
public AuthService(TransactionRunner tx, PasswordHasher hasher, Clock clock) {
public Account login(String email, char[] password) {
public long register(String name, String email, char[] password, VehicleInput vehicle, List<ServiceInput> history) {
public Account account(long owner) {
public void profile(long owner, long expected, String name, String email, char[] current, char[] next) {
```

## hr.unizd.autocare.service.CatalogService

File: `src/main/java/hr/unizd/autocare/service/CatalogService.java`

Declared types: CatalogService

```java
public CatalogService(TransactionRunner tx) {
public List<String> makes(int year) {
public List<String> models(int year, String make) {
public List<VariantRow> variants(int year, String make, String model, String search) {
public List<WorkRow> works(long owner, long vehicle, WorkCategory category) {
public List<WorkRow> onboardingWorks(long variant, WorkCategory category) {
```

## hr.unizd.autocare.service.DashboardService

File: `src/main/java/hr/unizd/autocare/service/DashboardService.java`

Declared types: DashboardService

```java
public DashboardService(TransactionRunner tx, Clock clock) {
public Dashboard get(long owner, long vehicle) {
```

## hr.unizd.autocare.service.MaintenanceService

File: `src/main/java/hr/unizd/autocare/service/MaintenanceService.java`

Declared types: MaintenanceService

```java
public MaintenanceService(TransactionRunner tx, Clock clock) {
public List<MaintenanceRow> list(long owner, long vehicle) {
```

## hr.unizd.autocare.service.Mapping

File: `src/main/java/hr/unizd/autocare/service/Mapping.java`

Declared types: Mapping

```java
```

## hr.unizd.autocare.service.PasswordHasher

File: `src/main/java/hr/unizd/autocare/service/PasswordHasher.java`

Declared types: PasswordHasher

```java
public String hash(char[] password) {
public boolean verify(char[] password, String encoded) {
```

## hr.unizd.autocare.service.ProblemService

File: `src/main/java/hr/unizd/autocare/service/ProblemService.java`

Declared types: ProblemService

```java
public ProblemService(TransactionRunner tx, DiagnosticStrategy strategy, Clock clock) {
public List<ProblemRow> list(long owner, long vehicle, ProblemStatus status) {
public Analysis analyze(long owner, long vehicle, String description) {
public long save(long owner, long vehicle, String requestKey, Analysis preview) {
public Long findSaved(long owner, String key) {
```

## hr.unizd.autocare.service.ServiceRecordService

File: `src/main/java/hr/unizd/autocare/service/ServiceRecordService.java`

Declared types: ServiceRecordService

```java
public ServiceRecordService(TransactionRunner tx, Clock clock) {
public long create(long owner, long vehicle, ServiceInput input) {
public static void validate(ServiceInput input, boolean historical, Clock clock) {
public List<ServiceRow> page(long owner, long vehicle, int offset) {
public ServiceDetail detail(long owner, long service) {
public Long findSaved(long owner, String key) {
```

## hr.unizd.autocare.service.TransactionRunner

File: `src/main/java/hr/unizd/autocare/service/TransactionRunner.java`

Declared types: TransactionRunner

```java
```

## hr.unizd.autocare.service.VehicleService

File: `src/main/java/hr/unizd/autocare/service/VehicleService.java`

Declared types: VehicleService

```java
public VehicleService(TransactionRunner tx, Clock clock) {
public List<VehicleRow> list(long owner) {
public VehicleRow active(long owner) {
public long add(long owner, VehicleInput input) {
public void update(long owner, long vehicle, long expected, VehicleInput input) {
public boolean identityEditable(long owner, long vehicle) {
public void activate(long owner, long vehicle) {
public void delete(long owner, long vehicle, Long replacement) {
```

## hr.unizd.autocare.strategy.DiagnosticStrategy

File: `src/main/java/hr/unizd/autocare/strategy/DiagnosticStrategy.java`

Declared types: DiagnosticStrategy

```java
```

## hr.unizd.autocare.strategy.KeywordDiagnosticStrategy

File: `src/main/java/hr/unizd/autocare/strategy/KeywordDiagnosticStrategy.java`

Declared types: KeywordDiagnosticStrategy

```java
public static String normalize(String value) {
```

## hr.unizd.autocare.tools.CsvReader

File: `src/main/java/hr/unizd/autocare/tools/CsvReader.java`

Declared types: CsvReader

```java
public CsvReader(Reader reader) {
public List<String> readRow()throws IOException {
```

## hr.unizd.autocare.tools.DatabaseTool

File: `src/main/java/hr/unizd/autocare/tools/DatabaseTool.java`

Declared types: DatabaseTool

```java
public static void run(String[] args) {
```

## hr.unizd.autocare.tools.DevelopmentSeed

File: `src/main/java/hr/unizd/autocare/tools/DevelopmentSeed.java`

Declared types: DevelopmentSeed

```java
public static void core(EntityManager em) {
public static void demo(EntityManager em) {
```

## hr.unizd.autocare.tools.ImagePathTool

File: `src/main/java/hr/unizd/autocare/tools/ImagePathTool.java`

Declared types: ImagePathTool

```java
public static void run(String[] args) {
```

## hr.unizd.autocare.tools.ReviewedIntervalTool

File: `src/main/java/hr/unizd/autocare/tools/ReviewedIntervalTool.java`

Declared types: ReviewedIntervalTool

```java
public static void run(String[] args) {
```

## hr.unizd.autocare.tools.SeedFiles

File: `src/main/java/hr/unizd/autocare/tools/SeedFiles.java`

Declared types: SeedFiles

```java
public static long read(Path file, Consumer<Map<String,String>> consumer) throws IOException {
public static void verifyManifest(Path dir) throws IOException {
public static String sha256(Path file) throws IOException {
public static String text(Map<String,String> row,String key,int max,boolean required) {
public static Integer integer(Map<String,String> row,String key,int min,int max) {
public static BigDecimal price(Map<String,String> row,String key) {
```

## hr.unizd.autocare.tools.SqlSeedTool

File: `src/main/java/hr/unizd/autocare/tools/SqlSeedTool.java`

Declared types: SqlSeedTool, BatchFailure

```java
public static void run(String[] args) {
public static void discover() throws SQLException {
public static void check() throws SQLException {
public static void validate(Path dir) throws IOException {
```

## hr.unizd.autocare.view.AnalysisDialog

File: `src/main/java/hr/unizd/autocare/view/AnalysisDialog.java`

Declared types: AnalysisDialog

```java
public final JTextArea description=new JTextArea(5, 45);
public final JButton analyze=Ui.button("Analiziraj", true), save=Ui.button("Spremi problem", true), cancel=Ui.button("Zatvori", false), check=Ui.button("Provjeri spremanje", false);
public final JLabel estimate=Ui.hint("Rezultat analize jos nije izracunat.");
public final DataTable<DiagnosticResult> results=new DataTable<>(new String[] {
public AnalysisDialog(Window owner) {
```

## hr.unizd.autocare.view.DashboardView

File: `src/main/java/hr/unizd/autocare/view/DashboardView.java`

Declared types: DashboardView

```java
public DashboardView() {
public void show(Dashboard d) {
```

## hr.unizd.autocare.view.LoginView

File: `src/main/java/hr/unizd/autocare/view/LoginView.java`

Declared types: LoginView

```java
public final JTextField email=new JTextField(25);
public final JPasswordField password=new JPasswordField(25);
public final JButton login=Ui.button("Prijavi se", true), register=Ui.button("Kreiraj racun", false);
public LoginView() {
```

## hr.unizd.autocare.view.MainFrame

File: `src/main/java/hr/unizd/autocare/view/MainFrame.java`

Declared types: MainFrame

```java
public final LoginView login=new LoginView();
public final DashboardView dashboard=new DashboardView();
public final VehiclesView vehicles=new VehiclesView();
public final MaintenanceView maintenance=new MaintenanceView();
public final ServicesView services=new ServicesView();
public final ProblemsView problems=new ProblemsView();
public final ProfileView profile=new ProfileView();
public final Map<String, JButton> navigation=new LinkedHashMap<>();
public final JButton refresh=Ui.button("Osvjezi", false);
public final JLabel status=Ui.hint("Spremno");
public MainFrame() {
public void auth() {
public void application() {
public void showPage(String name) {
public String page() {
public void context(VehicleRow v) {
```

## hr.unizd.autocare.view.MaintenanceView

File: `src/main/java/hr/unizd/autocare/view/MaintenanceView.java`

Declared types: MaintenanceView

```java
public final JButton estimate=Ui.button("Procijeni odabrana odrzavanja", false);
public final JLabel coverage=Ui.hint("Ucitajte odrzavanje.");
public final DataTable<MaintenanceRow> table=new DataTable<>(new String[] {
public MaintenanceView() {
```

## hr.unizd.autocare.view.OnboardingDialog

File: `src/main/java/hr/unizd/autocare/view/OnboardingDialog.java`

Declared types: OnboardingDialog

```java
public final JTextField name=new JTextField(25), email=new JTextField(25);
public final JPasswordField password=new JPasswordField(25), repeat=new JPasswordField(25);
public final VehicleForm vehicle=new VehicleForm();
public final JButton back=Ui.button("Natrag", false), next=Ui.button("Nastavi", true), finish=Ui.button("Zavrsi registraciju", true), cancel=Ui.button("Odustani", false), addHistory=Ui.button("Dodaj poznati servis", false), removeHistory=Ui.button("Ukloni odabrani servis", false);
public final DataTable<ServiceInput> history=new DataTable<>(new String[] {
public OnboardingDialog(Window owner) {
public int step() {
public void step(int step) {
public void clearPasswords() {
```

## hr.unizd.autocare.view.ProblemsView

File: `src/main/java/hr/unizd/autocare/view/ProblemsView.java`

Declared types: ProblemsView

```java
public final JComboBox<String> status=new JComboBox<>(new String[] {
public final JButton add=Ui.button("Analiziraj novi problem", true), detail=Ui.button("Detalj", false);
public final DataTable<ProblemRow> table=new DataTable<>(new String[] {
public ProblemsView() {
```

## hr.unizd.autocare.view.ProfileView

File: `src/main/java/hr/unizd/autocare/view/ProfileView.java`

Declared types: ProfileView

```java
public final JTextField name=new JTextField(25), email=new JTextField(25);
public final JPasswordField current=new JPasswordField(25), next=new JPasswordField(25), repeat=new JPasswordField(25);
public final JButton save=Ui.button("Spremi promjene", true), logout=Ui.button("Odjava", false);
public ProfileView() {
public void clearPasswords() {
```

## hr.unizd.autocare.view.ServiceEditorDialog

File: `src/main/java/hr/unizd/autocare/view/ServiceEditorDialog.java`

Declared types: ServiceEditorDialog

```java
public final DateField date=new DateField(LocalDate.now());
public final JTextArea note=new JTextArea(3, 25);
public final ServiceItemsModel items=new ServiceItemsModel();
public final JTable itemTable=new JTable(items);
public final JButton addMaintenance=Ui.button("Dodaj odrzavanje", false), addRepair=Ui.button("Dodaj popravak", false), remove=Ui.button("Ukloni odabranu stavku", false), save=Ui.button("Spremi servis", true), cancel=Ui.button("Odustani", false), check=Ui.button("Provjeri spremanje", false);
public ServiceEditorDialog(Window owner, int km, boolean historical, List<ProblemRow> problems) {
public int getRowCount() {
public int getColumnCount() {
public String getColumnName(int c) {
public Class<?> getColumnClass(int c) {
public Object getValueAt(int r, int c) {
public boolean isCellEditable(int r, int c) {
public void setValueAt(Object value, int r, int c) {
public ServiceInput input() {
public String requestKey() {
```

## hr.unizd.autocare.view.ServicesView

File: `src/main/java/hr/unizd/autocare/view/ServicesView.java`

Declared types: ServicesView

```java
public final JButton add=Ui.button("Novi servis", true), detail=Ui.button("Detalj", false), previous=Ui.button("Prethodna", false), next=Ui.button("Sljedeca", false);
public final JLabel page=Ui.hint("1");
public final DataTable<ServiceRow> table=new DataTable<>(new String[] {
public ServicesView() {
```

## hr.unizd.autocare.view.VehiclesView

File: `src/main/java/hr/unizd/autocare/view/VehiclesView.java`

Declared types: VehiclesView

```java
public final JButton add=Ui.button("Dodaj vozilo", true), edit=Ui.button("Uredi", false), activate=Ui.button("Aktiviraj", false), delete=Ui.button("Obrisi", false);
public final DataTable<VehicleRow> table=new DataTable<>(new String[] {
public VehiclesView() {
```

## hr.unizd.autocare.view.components.DataTable

File: `src/main/java/hr/unizd/autocare/view/components/DataTable.java`

Declared types: DataTable

```java
public DataTable(String[] columns, BiFunction<T, Integer, Object> value) {
public int getRowCount() {
public int getColumnCount() {
public String getColumnName(int c) {
public Object getValueAt(int r, int c) {
public Class<?> getColumnClass(int c) {
public void setRows(List<T> values) {
public List<T> rows() {
public T selected() {
public JTable table() {
public void filter(String text) {
```

## hr.unizd.autocare.view.components.DateField

File: `src/main/java/hr/unizd/autocare/view/components/DateField.java`

Declared types: DateField

```java
public DateField(LocalDate initial) {
public Object stringToValue(String text)throws ParseException {
public String valueToString(Object value) {
public LocalDate date() {
```

## hr.unizd.autocare.view.components.EstimateFormat

File: `src/main/java/hr/unizd/autocare/view/components/EstimateFormat.java`

Declared types: EstimateFormat

```java
public static BigDecimal rounded(BigDecimal amount) {
public static String display(BigDecimal amount) {
```

## hr.unizd.autocare.view.components.ServiceItemsModel

File: `src/main/java/hr/unizd/autocare/view/components/ServiceItemsModel.java`

Declared types: ServiceItemsModel, Line

```java
public int getRowCount() {
public int getColumnCount() {
public String getColumnName(int c) {
public Object getValueAt(int r, int c) {
public boolean isCellEditable(int r, int c) {
public void setValueAt(Object value, int row, int col) {
public void add(WorkRow work) {
public void remove(int row) {
public List<ItemInput> snapshot(boolean history) {
```

## hr.unizd.autocare.view.components.Ui

File: `src/main/java/hr/unizd/autocare/view/components/Ui.java`

Declared types: Ui, HintLabel

```java
public static final Color BACKGROUND=new Color(0xF4F7FB), INK=new Color(0x172B4D), ACCENT=new Color(0x176B87), MUTED=new Color(0x526477);
public static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.uuuu.").withResolverStyle(ResolverStyle.STRICT);
public static JPanel column() {
public static JPanel row(Component... controls) {
public static JPanel actions(Component... controls) {
public static JPanel card() {
public static JLabel heading(String title) {
public static JLabel hint(String text) {
public static JButton button(String title, boolean primary) {
public static JPanel form() {
public static void field(JPanel form, int row, String label, JComponent component) {
public static JSpinner mileage(int value) {
public static int integer(JSpinner s) {
public static BigDecimal parseMoney(String text, boolean optional) {
public static String money(BigDecimal value) {
public static String total(CostSummary c) {
public static String date(LocalDate d) {
public static String km(Integer km) {
public static String status(MaintenanceStatus s) {
public static String category(WorkCategory c) {
public static boolean confirm(Component parent, String text) {
public static void info(Component parent, String text) {
public static void error(Component parent, Throwable error) {
public static void escape(JDialog dialog, Runnable close) {
public static Map<Component, Boolean> disableTree(Component root) {
public static void restore(Map<Component, Boolean> old) {
```

## hr.unizd.autocare.view.components.VehicleForm

File: `src/main/java/hr/unizd/autocare/view/components/VehicleForm.java`

Declared types: VehicleForm

```java
public final JSpinner year=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1886, LocalDate.now().getYear(), 1));
public final JComboBox<String> make=new JComboBox<>(), model=new JComboBox<>();
public final JTextField search=new JTextField(18);
public final JButton find=Ui.button("Pretrazi varijante", false);
public final JSpinner mileage=Ui.mileage(0);
public final JLabel state=Ui.hint("Odaberite godinu, marku, model i tocnu varijantu.");
public final DataTable<VariantRow> variants=new DataTable<>(new String[] {
public VehicleForm() {
public VehicleInput input() {
public void existing(VehicleRow vehicle, boolean identityEditable) {
```

## hr.unizd.autocare.view.components.VehicleImage

File: `src/main/java/hr/unizd/autocare/view/components/VehicleImage.java`

Declared types: VehicleImage

```java
protected boolean removeEldestEntry(Map.Entry<String,ImageIcon> e) { return size()>64; }
public VehicleImage() {
public void showPath(String path) {
```

## hr.unizd.autocare.view.components.WorkPicker

File: `src/main/java/hr/unizd/autocare/view/components/WorkPicker.java`

Declared types: WorkPicker

```java
public static WorkRow choose(Component parent, List<WorkRow> rows) {
public void insertUpdate(DocumentEvent e) {
public void removeUpdate(DocumentEvent e) {
public void changedUpdate(DocumentEvent e) {
```
