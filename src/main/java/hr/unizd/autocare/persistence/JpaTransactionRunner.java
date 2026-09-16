package hr.unizd.autocare.persistence;

import hr.unizd.autocare.repository.Repositories;
import hr.unizd.autocare.service.TransactionRunner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

/**
 * JPA implementacija transakcijskog izvrÅ¡avanja.
 * Svi repositoryji jedne operacije koriste isti EntityManager.
 */
public final class JpaTransactionRunner implements TransactionRunner {

    private final EntityManagerFactory factory;

    public JpaTransactionRunner(EntityManagerFactory factory) {
        this.factory = factory;
    }

    @Override
    public <T> T read(Function<Repositories, T> action) {
        EntityManager entityManager = factory.createEntityManager();

        try {
            return action.apply(createRepositories(entityManager));
        } finally {
            entityManager.close();
        }
    }

    @Override
    public <T> T write(Function<Repositories, T> action) {
        EntityManager entityManager = factory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            T result = action.apply(createRepositories(entityManager));

            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private Repositories createRepositories(EntityManager entityManager) {
        return new Repositories(
                new JpaUserRepository(entityManager),
                new JpaVehicleRepository(entityManager),
                new JpaServiceRecordRepository(entityManager),
                new JpaProblemRepository(entityManager),
                new JpaCatalogRepository(entityManager));
    }
}
