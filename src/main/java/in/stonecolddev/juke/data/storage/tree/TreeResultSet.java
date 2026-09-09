package in.stonecolddev.juke.data.storage.tree;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TreeResultSet<T extends TreeRecord> {

  DatabaseTreeConfiguration configuration();

  T fromResultSet(ResultSet rs) throws SQLException;

  default ResultSetExtractor<Optional<DatabaseTree<T>>> resultSetExtractor() {
    return rs -> {
      List<T> nodes = new ArrayList<>();

      while (rs.next()) {
        nodes.add(fromResultSet(rs));
      }

      if (nodes.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(DatabaseTree.create(nodes));
    };
  }

}