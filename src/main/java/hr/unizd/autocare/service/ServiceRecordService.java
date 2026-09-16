package hr.unizd.autocare.service;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.model.Data.*;
import hr.unizd.autocare.repository.Repositories;
import java.time.*;
import java.util.*;
/** Spremanje servisa, stavki, kilometraze i rjesenja problema je jedna transakcija. */
public final class ServiceRecordService {
    private final TransactionRunner tx;
    private final Clock clock;
    public ServiceRecordService(TransactionRunner tx, Clock clock) {
        this.tx=tx;
        this.clock=clock;
    }
    /**
     * Sprema servis, stavke, rast kilometraze i rjesavanje problema atomarno.
     * Isti requestKey s istim sadrzajem vraca vec spremljeni ID.
     * @param owner ID prijavljenog korisnika; vlasnistvo se ponovno provjerava u transakciji
     * @param vehicle ID konkretnog vlastitog vozila
     * @param input nepromjenjivi snapshot lokalnog drafta
     * @return ID potvrdenog servisa; GUI zatim posebno ucitava svjezi prikaz
     * @throws AppException za nevaljan/vise nedostupan podatak ili nepoznat ishod commita
     */
    public long create(long owner, long vehicle, ServiceInput input) {
        return tx.write(r-> {
            r.users().lock(owner);
            validate(input, false, clock);
            Optional<ServiceRecord> existing=r.services().byRequest(owner, input.getRequestKey());
            if(existing.isPresent()) {
                if(!existing.get().getVehicle().getId().equals(vehicle))throw AppException.conflict("Ključ zahtjeva pripada drugom vozilu.");
                ensureSameRequest(r, owner, existing.get(), input);
                return existing.get().getId();
            }
            Vehicle v=r.vehicles().requireOwned(owner, vehicle);
            return saveInside(r, v, input, false, clock).getId();
        });
    }
    private static void ensureSameRequest(Repositories r, long owner, ServiceRecord saved, ServiceInput input) {
        boolean same=saved.getDate().equals(input.getDate())&&saved.getMileage()==input.getMileage()&&Objects.equals(saved.getNote(), Checks.optional(input.getNote(), 2000, "Napomena"))&&saved.getItems().size()==input.getItems().size();
        Map<Long, java.math.BigDecimal> requested=new HashMap<>();
        for(ItemInput i:input.getItems())requested.put(i.getWorkId(), i.getActualPrice());
        for(ServiceItem i:saved.getItems()) {
            java.math.BigDecimal expected=requested.get(i.getWork().getId());
            if(expected==null||i.getActualPrice()==null||expected.compareTo(i.getActualPrice())!=0)same=false;
        }
        if(r.problems().resolvedDescriptions(owner, saved.getId()).size()!=input.getResolvedProblemIds().size())same=false;
        for(Long id:input.getResolvedProblemIds()) {
            Problem p=r.problems().requireOwned(owner, id);
            if(p.getResolvedByService()==null||!p.getResolvedByService().getId().equals(saved.getId()))same=false;
        }
        if(!same)throw AppException.conflict("Isti zahtjev vec je spremljen s drugim podacima. Otvorite spremljeni servis; ne prepisujte povijest.");
    }
    static ServiceRecord saveInside(Repositories r, Vehicle vehicle, ServiceInput input, boolean historical, Clock clock) {
        validate(input, historical, clock);
        if(input.getDate().getYear()<vehicle.getYear())throw AppException.validation("Servis ne moze biti prije godine proizvodnje.");
        ServiceRecord record=new ServiceRecord(vehicle, input.getRequestKey(), input.getDate(), input.getMileage(), input.getNote());
        for(ItemInput i:input.getItems()) {
            WorkDefinition work=r.catalog().work(i.getWorkId());
            if(work.getCode().startsWith("OTHER_") && (input.getNote()==null || input.getNote().isBlank()))
                throw AppException.validation("Za drugi rad upisite stvarni opis zahvata u napomenu.");
            record.addItem(work,i.getActualPrice());
        }
        r.services().add(record);
        if(input.getMileage()>vehicle.getCurrentMileage())vehicle.updateMileage(input.getMileage());
        Set<Long> selected=new HashSet<>();
        for(Long id:input.getResolvedProblemIds()) {
            if(!selected.add(id))throw AppException.validation("Problem je odabran vise puta.");
            Problem p=r.problems().requireOwned(vehicle.getOwner().getId(), id);
            if(!Objects.equals(p.getVehicle().getId(), vehicle.getId()))throw AppException.validation("Problem pripada drugom vozilu.");
            p.resolve(record);
        }
        return record;
    }
    /** Provjerava lokalni ugovor unosa; vlasnistvo i stanje baze provjerava create.
     * @param input snapshot forme
     * @param historical dopusta nepoznatu cijenu samo u pocetnoj povijesti
     * @param clock izvor danasnjeg datuma, zamjenjiv u testu
     */
    public static void validate(ServiceInput input, boolean historical, Clock clock) {
        if(input==null || input.getDate()==null || input.getDate().isAfter(LocalDate.now(clock)))throw AppException.validation("Unesite datum koji nije u buducnosti.");
        Checks.mileage(input.getMileage());
        Checks.optional(input.getNote(), 2000, "Napomena");
        UUID.fromString(input.getRequestKey());
        if(input.getItems().isEmpty() || input.getItems().size()>100)throw AppException.validation("Servis treba imati 1 - 100 stavki.");
        Set<Long> seen=new HashSet<>();
        for(ItemInput i:input.getItems()) {
            if(!seen.add(i.getWorkId()))throw AppException.validation("Isti rad nije moguce dodati dvaput.");
            Checks.money(i.getActualPrice(), historical);
        }
        if(historical && !input.getResolvedProblemIds().isEmpty())throw AppException.validation("Pocetna povijest ne rjesava postojece probleme.");
    }
    public List<ServiceRow> page(long owner, long vehicle, int offset) {
        if(offset<0)throw AppException.validation("Nevaljana stranica.");
        return tx.read(r-> {
            r.vehicles().requireOwned(owner, vehicle);
            List<ServiceRow> rows=new ArrayList<>();
            for(ServiceRecord s:r.services().page(owner, vehicle, offset, 50))rows.add(Mapping.service(s));
            return List.copyOf(rows);
        });
    }
    public ServiceDetail detail(long owner, long service) {
        return tx.read(r-> {
            ServiceRecord s=r.services().requireOwned(owner, service);
            List<ItemRow> items=new ArrayList<>();
            for(ServiceItem i:s.getItems())items.add(new ItemRow(i.getWork().getName(), i.getWork().getCategory(), i.getActualPrice()));
            return new ServiceDetail(Mapping.service(s), items, r.problems().resolvedDescriptions(owner, service));
        });
    }
    public Long findSaved(long owner, String key) {
        return tx.read(r->r.services().byRequest(owner, key).map(ServiceRecord::getId).orElse(null));
    }
}
