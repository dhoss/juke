package in.stonecolddev.juke.data.storage.tree;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TreeResultSet2<T extends TreeRecord2> {

  DatabaseTreeConfiguration configuration();

  T fromResultSet(ResultSet rs) throws SQLException;

  default ResultSetExtractor<Optional<TreeRecord2>> resultSetExtractor() {
    return rs -> {
      List<TreeRecord2> nodes = new ArrayList<>();

      while (rs.next()) {
        nodes.add(fromResultSet(rs));
      }

      if (nodes.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(DatabaseTree2.createTree(nodes));
    };
  }

}