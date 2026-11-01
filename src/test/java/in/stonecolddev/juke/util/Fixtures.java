package in.stonecolddev.juke.util;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

public class Fixtures {


  public static class Database {

    private static final String username = "juke";
    private static final String password = "juke";
    private static final String databaseName = "juke";

    // TODO: consider using try with resources for creating postgres instance in testcontainers tests
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withInitScript("init.sql")
        .withEnv("TZ", "America/Denver")
        .withUsername(username)
        .withPassword(password)
        .withDatabaseName(databaseName);

    public static void startDatabase() {
      postgres.start();
      System.setProperty("spring.datasource.hikari.jdbc-url", postgres.getJdbcUrl());
      System.setProperty("spring.datasource.hikari.username", postgres.getUsername());
      System.setProperty("spring.datasource.hikari.password", postgres.getPassword());

      initDb();
    }

    public static void truncateDatabase() {
      flyway().clean();
      flyway().migrate();
    }

    private static Flyway flyway() {
      return Flyway.configure()
          .cleanDisabled(false)
          .locations("classpath:db/migrations")
          .dataSource(postgres.getJdbcUrl(), username, password)
          .load();
    }

    private static void initDb() {
      flyway().migrate();
    }
  }

}