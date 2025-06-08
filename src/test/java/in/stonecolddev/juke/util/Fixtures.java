package in.stonecolddev.juke.util;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class Fixtures {


  public static class Database {

    private static final String username = "juke";
    private static final String password = "juke";
    private static final String databaseName = "juke";

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withUsername(username)
        .withPassword(password)
        .withDatabaseName(databaseName);

    public static void startDatabase() {
      postgres.start();
      System.setProperty("spring.datasource.hikari.jdbc-url", postgres.getJdbcUrl());
      System.setProperty("spring.datasource.hikari.username", postgres.getUsername());
      System.setProperty("spring.datasource.hikari.password", postgres.getPassword());

      initDb(postgres.getJdbcUrl(), username, password);
    }

    public static void stopDatabase() {
      postgres.stop();
    }

    public static void initDb(String jdbcUrl, String username, String password) {

      Flyway flyway = Flyway.configure()
          .locations("classpath:db/migrations")
          .dataSource(jdbcUrl, username, password)
          .load();

      flyway.migrate();
    }
  }

}