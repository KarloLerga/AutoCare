package hr.unizd.autocare.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** SQL Server connection settings, separate from JPA so developer imports reuse secure JDBC. */
public final class SqlSettings {
    private final String host, database, user, password;
    private final int port;
    private SqlSettings(String host, int port, String database, String user, String password) {
        this.host=host; this.port=port; this.database=database; this.user=user; this.password=password;
    }
    public static SqlSettings environment(boolean test) {
        return from(System.getenv(), test, false);
    }
    public static SqlSettings discovery() {
        return from(System.getenv(), false, true);
    }
    /** Pure validation, also used by offline tests. Password is never part of the URL or toString. */
    public static SqlSettings from(Map<String,String> env, boolean test, boolean discovery) {
        String prefix=test?"AUTOCARE_TEST_":"AUTOCARE_DB_";
        String host=required(env,prefix+"HOST");
        if(!host.matches("[a-zA-Z0-9][a-zA-Z0-9.-]{0,252}"))throw new IllegalArgumentException("Nevaljan SQL host.");
        int port=Integer.parseInt(env.getOrDefault(prefix+"PORT","1433"));
        if(port<1||port>65535)throw new IllegalArgumentException("Nevaljan SQL port.");
        String database=discovery?"master":required(env,prefix+"NAME");
        // A database name is a JDBC property; deliberately reject URL separators and control characters.
        if(!database.matches("[a-zA-Z0-9_][a-zA-Z0-9_. -]{0,127}"))throw new IllegalArgumentException("Nevaljan naziv baze (host nije naziv baze).");
        if(!discovery&&Set.of("master","tempdb","model","msdb").contains(database.toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Aplikacija ne smije koristiti sistemsku bazu.");
        if(test) {
            if(!database.endsWith("_test"))throw new IllegalArgumentException("Test zahtijeva zasebnu bazu s nastavkom _test.");
            if(database.equals(env.get("AUTOCARE_DB_NAME")))throw new IllegalArgumentException("Testna i aplikacijska baza moraju biti odvojene.");
        }
        return new SqlSettings(host,port,database,required(env,prefix+"USER"),required(env,prefix+"PASSWORD"));
    }
    public String database() { return database; }
    public String host() { return host; }
    public String user() { return user; }
    public String password() { return password; }
    public String url() {
        return "jdbc:sqlserver://"+host+":"+port+";databaseName="+database+
            ";encrypt=true;trustServerCertificate=false;loginTimeout=60;socketTimeout=120000;applicationName=AutoCare;";
    }
    public Connection connect(boolean importer) throws SQLException {
        Properties p=new Properties(); p.setProperty("user",user); p.setProperty("password",password);
        // SQL authentication: no integrated-security DLL or Azure API credentials are required.
        if(importer) { p.setProperty("useBulkCopyForBatchInsert","true"); p.setProperty("bulkCopyForBatchInsertBatchSize","1000"); }
        return DriverManager.getConnection(url(),p);
    }
    public void requireSchemaConsent(Map<String,String> env) {
        if(!database.equals(env.get("AUTOCARE_SCHEMA_TARGET")))
            throw new IllegalArgumentException("Za Hibernate update postavite AUTOCARE_SCHEMA_TARGET na tocno ime odabrane razvojne baze. Baza se ne stvara automatski.");
    }
    private static String required(Map<String,String> env,String key) {
        String v=env.get(key); if(v==null||v.isBlank())throw new IllegalArgumentException("Nedostaje "+key+"."); return v;
    }
    @Override public String toString() { return "SQL Server "+host+":"+port+" / "+database; }
}
