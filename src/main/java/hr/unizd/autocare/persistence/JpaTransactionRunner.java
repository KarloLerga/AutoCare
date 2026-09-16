package hr.unizd.autocare.persistence;
import jakarta.persistence.*;
import hr.unizd.autocare.repository.*;
import hr.unizd.autocare.service.*;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.logging.*;
/** Posjeduje EM samo za vrijeme jedne operacije. Repositoryji ne upravljaju transakcijom. */
public final class JpaTransactionRunner implements TransactionRunner {
    private static final Logger LOG=Logger.getLogger(JpaTransactionRunner.class.getName());
    private final EntityManagerFactory factory;
    public JpaTransactionRunner(EntityManagerFactory factory) {
        this.factory=factory;
    }
    public <T>T read(Function<Repositories, T> action) {
        return run(action, false);
    }
    public <T>T write(Function<Repositories, T> action) {
        return run(action, true);
    }
    private <T>T run(Function<Repositories, T> action, boolean write) {
        EntityManager em=null;
        EntityTransaction tx=null;
        boolean committing=false, committed=false;
        try {
            em=factory.createEntityManager();
            tx=em.getTransaction();
            tx.begin();
            Repositories repos=new Repositories(new JpaUserRepository(em), new JpaVehicleRepository(em), new JpaServiceRecordRepository(em), new JpaProblemRepository(em), new JpaCatalogRepository(em));
            T result=action.apply(repos);
            if(write)em.flush();
            committing=true;
            tx.commit();
            committed=true;
            return result;
        }
        catch(RuntimeException error) {
            if(tx!=null && tx.isActive())try {
                tx.rollback();
            }
            catch(RuntimeException rollback) {
                error.addSuppressed(rollback);
            }
            if(committing && write)throw new AppException(AppException.Kind.COMMIT_UNKNOWN, "Veza je prekinuta tijekom potvrde. Provjerite je li zapis spremljen prije ponavljanja.", error);
            if(error instanceof AppException)throw error;
            if(error instanceof IllegalArgumentException)throw AppException.validation(error.getMessage());
            if(error instanceof OptimisticLockException || error instanceof PessimisticLockException || error instanceof LockTimeoutException)throw new AppException(AppException.Kind.CONFLICT, "Podaci su promijenjeni ili zauzeti. Osvjezite prikaz.", error);
            for(Throwable cause=error; cause!=null; cause=cause.getCause())if(cause instanceof SQLException sql && sql.getSQLState()!=null && sql.getSQLState().startsWith("23"))throw new AppException(AppException.Kind.CONFLICT, "Zapis vec postoji ili vise nije moguce izvrsiti promjenu. Osvjezite podatke.", error);
            throw new AppException(AppException.Kind.DATABASE, "Baza trenutačno nije dostupna ili operacija nije uspjela. Provjerite vezu i zapisnik.", error);
        }
        finally {
            if(em!=null)try {
                em.close();
            }
            catch(RuntimeException close) {
                LOG.log(Level.WARNING, committed?"Commit je uspio; zatvaranje EM-a nije uspjelo.":"Zatvaranje EM-a nije uspjelo.", close);
            }
        }
    }
}
