package hr.unizd.autocare.service;
import hr.unizd.autocare.repository.Repositories;
import java.util.function.Function;
/** Jedna kratka transakcija za cijeli use-case, bez ugnijezdenih poziva. */
public interface TransactionRunner {
    <T> T read(Function<Repositories, T> action);
    <T> T write(Function<Repositories, T> action);
}
