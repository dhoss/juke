package in.stonecolddev.juke.util;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class Fixtures {


  public static class Database {

    public static String username = "juke";
    public static String password = "juke";
    public static String databaseName = "juke";

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withUsername(username)
        .withPassword(password)
        .withDatabaseName(databaseName);

    public static void initDb(String jdbcUrl, String username, String password) {

      Flyway flyway = Flyway.configure()
          .locations("classpath:db/migrations")
          .dataSource(jdbcUrl, username, password)
          .load();

      flyway.migrate();
    }
  }

}