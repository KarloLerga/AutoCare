package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.VariantRow;
import hr.unizd.autocare.model.Data.WorkRow;
import hr.unizd.autocare.repository.Repositories;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** ÄŒitanje kataloga vozila i radova za GUI. */
public final class CatalogService {

    private final TransactionRunner transactions;

    public CatalogService(TransactionRunner transactions) {
        this.transactions = transactions;
    }

    public List<String> makes(int year) {
        return transactions.read(
                repositories -> repositories.catalog().makes(year));
    }

    public List<String> models(
            int year,
            String make) {

        return transactions.read(
                repositories ->
                        repositories.catalog().models(year, make));
    }

    public List<VariantRow> variants(
            int year,
            String make,
            String model,
            String search) {

        return transactions.read(repositories -> {
            List<VariantRow> rows = new ArrayList<>();

            for (VehicleVariant variant
                    : repositories.catalog().variants(
                            year,
                            make,
                            model,
                            search)) {

                rows.add(Mapping.variant(variant));
            }

            return rows;
        });
    }

    public List<WorkRow> works(
            long ownerId,
            long vehicleId,
            WorkCategory category) {

        return transactions.read(repositories -> {
            Vehicle vehicle =
                    repositories.vehicles()
                            .requireOwned(ownerId, vehicleId);

            return workRows(
                    repositories,
                    vehicle.getVariant().getId(),
                    category);
        });
    }

    public List<WorkRow> onboardingWorks(
            long variantId,
            WorkCategory category) {

        return transactions.read(repositories -> {
            repositories.catalog().variant(variantId);
            return workRows(repositories, variantId, category);
        });
    }

    static List<WorkRow> workRows(
            Repositories repositories,
            long variantId,
            WorkCategory category) {

        Map<Long, VehicleWorkRule> rules = new HashMap<>();

        for (VehicleWorkRule rule
                : repositories.catalog().rules(variantId)) {

            rules.put(rule.getWork().getId(), rule);
        }

        List<WorkRow> rows = new ArrayList<>();

        for (WorkDefinition work
                : repositories.catalog().works(category)) {

            VehicleWorkRule rule = rules.get(work.getId());

            if (!appliesWithoutSpecificRule(work, rule)) {
                continue;
            }

            BigDecimal price;
            String priceNote;

            if (rule != null) {
                price = rule.getEstimatedPrice();
                priceNote = rule.getEstimateNote();
            } else {
                price = work.getDefaultEstimatedPrice();
                priceNote = work.getEstimateNote();
            }

            rows.add(
                    new WorkRow(
                            work.getId(),
                            work.getCode(),
                            work.getName(),
                            work.getCategory(),
                            price,
                            priceNote));
        }

        return rows;
    }

    /**
     * Ako nema specifiÄnog pravila, WorkDefinition se koristi samo kada
     * stvarno ima neku zadanu vrijednost ili predstavlja ruÄni OTHER unos.
     */
    private static boolean appliesWithoutSpecificRule(
            WorkDefinition work,
            VehicleWorkRule rule) {

        if (rule != null) {
            return true;
        }

        if (work.getCode().startsWith("OTHER_")) {
            return true;
        }

        return work.getDefaultIntervalKm() != null
                || work.getDefaultIntervalMonths() != null
                || work.getDefaultEstimatedPrice() != null;
    }
}
