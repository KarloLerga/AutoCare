package hr.unizd.autocare.tools;
import jakarta.persistence.*;
import hr.unizd.autocare.domain.*;
import java.math.BigDecimal;
import java.util.*;
/** Developerski seed; sintetska vozila i njihove cijene/intervali nisu stvarne preporuke. */
public final class DevelopmentSeed {
    private DevelopmentSeed() {
    }
    public static void core(EntityManager em) {
        String[][] works= {
            {
                "ENGINE_OIL", "Motorno ulje", "MAINTENANCE"
            }, {
                "OIL_FILTER", "Filtar ulja", "MAINTENANCE"
            }, {
                "AIR_FILTER", "Filtar zraka", "MAINTENANCE"
            }, {
                "CABIN_FILTER", "Filtar kabine", "MAINTENANCE"
            }, {
                "FUEL_FILTER", "Filtar goriva", "MAINTENANCE"
            }, {
                "BRAKE_FLUID", "Kociona tekucina", "MAINTENANCE"
            }, {
                "COOLANT", "Rashladna tekucina", "MAINTENANCE"
            }, {
                "TIMING_KIT", "Zupcasti set i prateci dijelovi", "MAINTENANCE"
            }, {
                "FRONT_BRAKES", "Prednje kocione plocice", "REPAIR"
            }, {
                "AC_COMPRESSOR", "Kompresor klime", "REPAIR"
            }, {
                "RADIATOR_FAN", "Ventilator hladnjaka", "REPAIR"
            }, {
                "AC_SERVICE", "Servis klima sustava", "REPAIR"
            }, {
                "BATTERY", "Akumulator", "REPAIR"
            }, {
                "GLOW_PLUGS", "Grijaci dizelskog motora", "REPAIR"
            }, {
                "DIAGNOSIS", "Dijagnosticki pregled", "REPAIR"
            }
        };
        for(String[] row:works)if(work(em, row[0])==null)em.persist(new WorkDefinition(row[0], row[1], WorkCategory.valueOf(row[2]), null, null));
        em.flush();
        String[][] rules= {
            {
                "ac-warm", "AC_COMPRESSOR", "slabo hladi", "4"
            }, {
                "ac-noise", "AC_COMPRESSOR", "zvizdi", "2"
            }, {
                "fan-idle", "RADIATOR_FAN", "u leru", "3"
            }, {
                "fan-heat", "RADIATOR_FAN", "pregrijava", "5"
            }, {
                "ac-fluid", "AC_SERVICE", "slabo hladi", "2"
            }, {
                "bat-start", "BATTERY", "tesko pali", "2"
            }, {
                "bat-slow", "BATTERY", "sporo vergla", "5"
            }, {
                "glow-cold", "GLOW_PLUGS", "hladan", "3"
            }, {
                "glow-start", "GLOW_PLUGS", "tesko pali", "2"
            }
        };
        for(String[] row:rules)if(em.createQuery("select count(r) from DiagnosticRule r where r.code=:code", Long.class).setParameter("code", row[0]).getSingleResult()==0)em.persist(new DiagnosticRule(row[0], work(em, row[1]), row[2], Integer.parseInt(row[3])));
    }
    public static void demo(EntityManager em) {
        core(em);
        for(int i=1; i<=2; i++) {
            String code="demo-vehicle-"+i;
            VehicleVariant v=em.createQuery("select v from VehicleVariant v where v.code=:code", VehicleVariant.class).setParameter("code", code).getResultStream().findFirst().orElse(null);
            if(v==null) {
                v=new VehicleVariant(code, "DEMO", "Vozilo "+i, "Sintetska generacija", "Testni motor "+i, 2010, 2026, i==1?"Diesel":"Petrol");
                em.persist(v);
            }
            em.flush();
            String[] codes= {
                "ENGINE_OIL", "OIL_FILTER", "AIR_FILTER", "AC_COMPRESSOR", "RADIATOR_FAN", "BATTERY", "GLOW_PLUGS"
            };
            for(String workCode:codes) {
                if(i==2&&workCode.equals("GLOW_PLUGS"))continue;
                WorkDefinition w=work(em, workCode);
                long count=em.createQuery("select count(r) from VehicleWorkRule r where r.variant.id=:v and r.work.id=:w", Long.class).setParameter("v", v.getId()).setParameter("w", w.getId()).getSingleResult();
                if(count==0) {
                    boolean maintenance=w.getCategory()==WorkCategory.MAINTENANCE;
                    String note="DEMO: sinteticki podatak za testiranje, nije cijena hrvatskog trzista niti preporuka za stvarno vozilo.";
                    em.persist(new VehicleWorkRule(v, w, maintenance?10000:null, maintenance?12:null, new BigDecimal(maintenance?"50.00":"200.00"), maintenance?note:null, note));
                }
            }
        }
    }
    private static WorkDefinition work(EntityManager em, String code) {
        return em.createQuery("select w from WorkDefinition w where w.code=:code", WorkDefinition.class).setParameter("code", code).getResultStream().findFirst().orElse(null);
    }
}
