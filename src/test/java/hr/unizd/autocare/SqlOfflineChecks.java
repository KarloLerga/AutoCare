package hr.unizd.autocare;
import hr.unizd.autocare.app.SqlSettings;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.view.components.EstimateFormat;
import hr.unizd.autocare.tools.SeedFiles;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
/** Real JDK-only checks; no SQL server, JDBC driver or ORM is contacted here. */
public final class SqlOfflineChecks {
    private static int checks;
    private static void check(boolean condition){checks++;if(!condition)throw new AssertionError("Offline check "+checks);}
    private static void rejects(Runnable r){checks++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Expected input rejection");}
    public static void run() {
        Map<String,String> env=new HashMap<>(Map.of("AUTOCARE_DB_HOST","unit.database.windows.net","AUTOCARE_DB_PORT","1433","AUTOCARE_DB_NAME","unit_db","AUTOCARE_DB_USER","test","AUTOCARE_DB_PASSWORD","fictional-test-value"));
        SqlSettings s=SqlSettings.from(env,false,false);
        check(s.url().startsWith("jdbc:sqlserver://"));check(s.url().contains("encrypt=true"));check(s.url().contains("trustServerCertificate=false"));check(!s.url().contains("fictional-test-value"));check(!s.toString().contains("fictional-test-value"));
        rejects(()->s.requireSchemaConsent(env));env.put("AUTOCARE_SCHEMA_TARGET","unit_db");s.requireSchemaConsent(env);check(true);
        env.put("AUTOCARE_DB_NAME","master");rejects(()->SqlSettings.from(env,false,false));check(SqlSettings.from(env,false,true).database().equals("master"));
        env.put("AUTOCARE_DB_NAME","x;encrypt=false");rejects(()->SqlSettings.from(env,false,false));env.put("AUTOCARE_DB_NAME","");rejects(()->SqlSettings.from(env,false,false));
        env.put("AUTOCARE_DB_NAME","unit_db");env.put("AUTOCARE_DB_PORT","0");rejects(()->SqlSettings.from(env,false,false));env.put("AUTOCARE_DB_PORT","1433");
        check(EstimateFormat.rounded(new BigDecimal("284")).equals(new BigDecimal("280")));check(EstimateFormat.rounded(new BigDecimal("285")).equals(new BigDecimal("290")));check(EstimateFormat.display(null).equals("Nema procjene"));rejects(()->EstimateFormat.rounded(new BigDecimal("-1")));
        check(Checks.money(new BigDecimal("53.47"),false).equals(new BigDecimal("53.47")));
        check(EstimateSelection.conflicts(Set.of("OIL_SERVICE"),"OIL_FILTER").contains("OIL_SERVICE"));check(EstimateSelection.conflicts(Set.of("CABIN_FILTER"),"OIL_SERVICE").isEmpty());
        rejects(()->SeedFiles.price(Map.of("p","284.00"),"p"));check(SeedFiles.price(Map.of("p","280.00"),"p").equals(new BigDecimal("280.00")));check(SeedFiles.price(Map.of("p",""),"p")==null);
        MaintenanceCalculator c=new MaintenanceCalculator();LocalDate now=LocalDate.of(2026,9,16);
        check(c.calculate(ScheduleKind.CONDITION_BASED,null,null,null,null,90000,now)==MaintenanceStatus.CONDITION_BASED);
        check(c.calculate(ScheduleKind.VEHICLE_INDICATOR,null,null,null,null,90000,now)==MaintenanceStatus.VEHICLE_INDICATOR);
        check(c.calculate(ScheduleKind.UNKNOWN,null,null,null,null,90000,now)==MaintenanceStatus.UNKNOWN_INTERVAL);
        System.out.println("Additional offline checks passed: "+checks);
    }
    public static void main(String[] args){run();}
}
