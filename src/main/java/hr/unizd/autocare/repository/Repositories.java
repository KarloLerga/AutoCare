package hr.unizd.autocare.repository;
/** Pet repozitorija vezanih uz isti persistence context. */
public final class Repositories {
    private final UserRepository users;
    private final VehicleRepository vehicles;
    private final ServiceRecordRepository services;
    private final ProblemRepository problems;
    private final CatalogRepository catalog;
    public Repositories(UserRepository users, VehicleRepository vehicles, ServiceRecordRepository services, ProblemRepository problems, CatalogRepository catalog) {
        this.users=users;
        this.vehicles=vehicles;
        this.services=services;
        this.problems=problems;
        this.catalog=catalog;
    }
    public UserRepository users() {
        return users;
    }
    public VehicleRepository vehicles() {
        return vehicles;
    }
    public ServiceRecordRepository services() {
        return services;
    }
    public ProblemRepository problems() {
        return problems;
    }
    public CatalogRepository catalog() {
        return catalog;
    }
}
