package in.stonecolddev.juke.util;


import org.flywaydb.core.Flyway;

public class DbMigrations {

  public static void initDb(String jdbcUrl, String username, String password) {

    Flyway flyway = Flyway.configure()
        .locations("classpath:db/migrations")
        .dataSource(jdbcUrl, username, password)
        .load();

    flyway.migrate();
  }
}