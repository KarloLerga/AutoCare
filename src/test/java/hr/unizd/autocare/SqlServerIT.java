package hr.unizd.autocare;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import hr.unizd.autocare.app.DatabaseConfig;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.persistence.JpaTransactionRunner;
import hr.unizd.autocare.service.*;
import hr.unizd.autocare.tools.DevelopmentSeed;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
/** Eksplicitni Maven profil sqlserver-it; zahtijeva zasebnu bazu cije ime zavrsava na _test. */
class SqlServerIT {
    @Test void serviceRollbackIdempotencyAndOwnership() {
        try(EntityManagerFactory emf=DatabaseConfig.open("update", true)) {
            try(EntityManager em=emf.createEntityManager()) {
                em.getTransaction().begin();
                DevelopmentSeed.demo(em);
                em.getTransaction().commit();
            }
            TransactionRunner tx=new JpaTransactionRunner(emf);
            AuthService auth=new AuthService(tx, new PasswordHasher(), Clock.systemUTC());
            VehicleService vehicles=new VehicleService(tx, Clock.systemUTC());
            ServiceRecordService services=new ServiceRecordService(tx, Clock.systemUTC());
            long variant, work;
            try(EntityManager em=emf.createEntityManager()) {
                variant=em.createQuery("select v.id from VehicleVariant v where v.code='demo-vehicle-1'", Long.class).getSingleResult();
                work=em.createQuery("select w.id from WorkDefinition w where w.code='ENGINE_OIL'", Long.class).getSingleResult();
            }
            long owner=auth.register("Integration test", "it-"+UUID.randomUUID()+"@example.com", "temporary-test-password".toCharArray(), new VehicleInput(variant, 2017, 100000), List.of());
            try {
                VehicleRow vehicle=vehicles.active(owner);
                assertEquals(1, vehicles.list(owner).size());
                assertThrows(AppException.class, ()->vehicles.delete(owner, vehicle.getId(), null));
                ServiceInput bad=new ServiceInput(UUID.randomUUID().toString(), LocalDate.now(Clock.systemUTC()), 120000, null, List.of(new ItemInput(work, new BigDecimal("50.00"))), List.of(Long.MAX_VALUE));
                assertThrows(AppException.class, ()->services.create(owner, vehicle.getId(), bad));
                assertEquals(100000, vehicles.active(owner).getMileage());
                assertTrue(services.page(owner, vehicle.getId(), 0).isEmpty());
                ServiceInput good=new ServiceInput(UUID.randomUUID().toString(), LocalDate.now(Clock.systemUTC()), 120000, "integration", List.of(new ItemInput(work, new BigDecimal("50.00"))), List.of());
                long id=services.create(owner, vehicle.getId(), good);
                assertEquals(id, services.create(owner, vehicle.getId(), good));
                assertEquals(1, services.page(owner, vehicle.getId(), 0).size());
                assertEquals(120000, vehicles.active(owner).getMileage());
                assertEquals(new BigDecimal("50.00"), services.detail(owner, id).getHeader().getTotal().getKnownTotal());
                assertThrows(AppException.class, ()->services.detail(Long.MAX_VALUE, id));
            }
            finally {
                cleanup(emf, owner);
            }
        }
    }
    private void cleanup(EntityManagerFactory emf, long owner) {
        try(EntityManager em=emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("update User u set u.activeVehicle=null where u.id=:o").setParameter("o", owner).executeUpdate();
            em.createQuery("delete from Problem p where p.vehicle.owner.id=:o").setParameter("o", owner).executeUpdate();
            em.createQuery("delete from ServiceItem i where i.serviceRecord.vehicle.owner.id=:o").setParameter("o", owner).executeUpdate();
            em.createQuery("delete from ServiceRecord s where s.vehicle.owner.id=:o").setParameter("o", owner).executeUpdate();
            em.createQuery("delete from Vehicle v where v.owner.id=:o").setParameter("o", owner).executeUpdate();
            em.createQuery("delete from User u where u.id=:o").setParameter("o", owner).executeUpdate();
            em.getTransaction().commit();
        }
    }
}
