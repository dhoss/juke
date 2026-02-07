package in.stonecolddev.juke.forum.storage;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@SuppressWarnings("SqlSourceToSinkFlow")
@Component
public class JdbcTemplateDatabaseHandler implements DatabaseHandler {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public JdbcTemplateDatabaseHandler(
      NamedParameterJdbcTemplate jdbcTemplate
  ) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public <T> T runQuery(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> resultSetExtractor) {
    return jdbcTemplate.query(sql, paramMap, resultSetExtractor);
  }

  @Override
  public <T> List<T> runQuery(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
    return jdbcTemplate.query(sql, paramMap, rowMapper);
  }

}