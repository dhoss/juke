package in.stonecolddev.juke.util;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


// credit: https://github.com/testcontainers/testcontainers-java/blob/b64d3bbf5c2db8c7c87484f6a1f92aba6202d692/modules/jdbc-test/src/main/java/org/testcontainers/db/AbstractContainerDatabaseTest.java
public abstract class AbstractDatabaseTest {

  protected ResultSet performReadQuery(JdbcDatabaseContainer<?> container, String sql) throws SQLException {
    DataSource ds = getDataSource(container);
    ResultSet resultSet;
    try (Statement statement = ds.getConnection().createStatement()) {
      statement.execute(sql);
      resultSet = statement.getResultSet();
    }

    resultSet.next();
    return resultSet;
  }

  protected void performWriteQuery(JdbcDatabaseContainer<?> container, String sql) throws SQLException {
    DataSource ds = getDataSource(container);
    try (Statement statement = ds.getConnection().createStatement()) {
      statement.execute(sql);
    }
  }

  protected DataSource getDataSource(JdbcDatabaseContainer<?> container) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(container.getJdbcUrl());
    hikariConfig.setUsername(container.getUsername());
    hikariConfig.setPassword(container.getPassword());
    hikariConfig.setDriverClassName(container.getDriverClassName());
    return new HikariDataSource(hikariConfig);
  }
}
