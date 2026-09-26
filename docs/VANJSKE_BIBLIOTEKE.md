# Vanjske biblioteke i alati

Verzije su preuzete iz trenutnog `pom.xml` projekta.

| Komponenta | Verzija | Zašto se koristi | Službeni izvor |
|---|---:|---|---|
| Jakarta Persistence API | 3.2.0 | Standardni JPA API | https://jakarta.ee/specifications/persistence/3.2/ |
| Hibernate ORM | 7.4.8.Final | JPA provider i ORM | https://hibernate.org/orm/documentation/7.4/ |
| Microsoft JDBC Driver for SQL Server | 13.4.0.jre11 | JDBC veza prema Azure SQL / SQL Server bazi | https://learn.microsoft.com/sql/connect/jdbc/ |
| FlatLaf | 3.6.2 | Moderniji Swing Look and Feel | https://www.formdev.com/flatlaf/ |
| Ikonli Swing | 12.4.0 | Integracija ikona sa Swingom | https://kordamp.org/ikonli/ |
| Ikonli FontAwesome 6 pack | 12.4.0 | FontAwesome ikone | https://kordamp.org/ikonli/ |
| Apache Maven | Maven Wrapper | Build i upravljanje zavisnostima | https://maven.apache.org/ |

## Maven build pluginovi

- Maven Compiler Plugin 3.14.1 - kompajliranje uz Java release 25.
- Maven JAR Plugin 3.4.2 - izrada JAR-a s `hr.unizd.autocare.app.Main` kao main klasom.
- Maven Dependency Plugin 3.8.1 - kopiranje runtime biblioteka u `target/lib`.
- Maven Javadoc Plugin 3.12.0 - generiranje HTML API dokumentacije.

Službena dokumentacija Maven Javadoc Plugina: https://maven.apache.org/plugins/maven-javadoc-plugin/.
