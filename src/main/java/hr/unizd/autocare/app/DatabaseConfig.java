package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** One application-scoped EMF; short-lived EntityManagers are created by the transaction runner. */
public final class DatabaseConfig {
    private DatabaseConfig() { }
    public static EntityManagerFactory open(String schemaAction, boolean test) {
        if(!Set.of("validate","update").contains(schemaAction))throw new IllegalArgumentException("Dopusteni su validate ili eksplicitni update.");
        SqlSettings settings=SqlSettings.environment(test);
        if(schemaAction.equals("update"))settings.requireSchemaConsent(System.getenv());
        Map<String,Object> p=new HashMap<>();
        p.put("jakarta.persistence.jdbc.url",settings.url());
        p.put("jakarta.persistence.jdbc.user",settings.user());
        p.put("jakarta.persistence.jdbc.password",settings.password());
        p.put("jakarta.persistence.jdbc.driver","com.microsoft.sqlserver.jdbc.SQLServerDriver");
        p.put("hibernate.dialect","org.hibernate.dialect.SQLServerDialect");
        p.put("hibernate.default_schema","dbo");
        p.put("hibernate.use_nationalized_character_data","true");
        p.put("hibernate.hbm2ddl.auto",schemaAction);
        p.put("hibernate.hbm2ddl.halt_on_error","true");
        p.put("hibernate.connection.provider_class","org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        p.put("hibernate.hikari.maximumPoolSize","2");
        p.put("hibernate.hikari.minimumIdle","0");
        p.put("hibernate.hikari.idleTimeout","30000");
        p.put("hibernate.hikari.keepaliveTime","0");
        p.put("hibernate.hikari.connectionTimeout","120000");
        p.put("hibernate.hikari.validationTimeout","5000");
        p.put("hibernate.hikari.maxLifetime","300000");
        return Persistence.createEntityManagerFactory("autocare",p);
    }
}
