package in.stonecolddev.juke.forum.storage;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

public interface DatabaseHandler {

  <T> List<T> runQuery(String query, Map<String, ?> paramMap, RowMapper<T> rowMapper);

  <T> T runQuery(String query, Map<String, ?> paramMap, ResultSetExtractor<T> resultSetExtractor);

}