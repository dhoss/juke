package in.stonecolddev.juke.util;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


// credit: https://github.com/testcontainers/testcontainers-java/blob/b64d3bbf5c2db8c7c87484f6a1f92aba6202d692/modules/jdbc-test/src/main/java/org/testcontainers/db/AbstractContainerDatabaseTest.java
public abstract class AbstractDatabaseTest {

  protected static ResultSet performReadQuery(String sql) throws SQLException {
    DataSource ds = getDataSource();
    ResultSet resultSet;
    Statement statement = ds.getConnection().createStatement();
    statement.execute(sql);
    resultSet = statement.getResultSet();
    //resultSet.next();
    return resultSet;
  }

  protected void performWriteQuery(String sql) throws SQLException {
    DataSource ds = getDataSource();
    try (Statement statement = ds.getConnection().createStatement()) {
      statement.execute(sql);
    }
  }

  protected static DataSource getDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    PostgreSQLContainer<?> postgres = Fixtures.Database.postgres;
    hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
    hikariConfig.setUsername(postgres.getUsername());
    hikariConfig.setPassword(postgres.getPassword());
    hikariConfig.setDriverClassName(postgres.getDriverClassName());
    return new HikariDataSource(hikariConfig);
  }
}
