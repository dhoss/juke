package in.stonecolddev.juke.data.storage.tree;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static in.stonecolddev.juke.data.storage.tree.DatabaseTree.createTree;
import static in.stonecolddev.juke.data.storage.tree.DatabaseTree.findRootId;

public interface TreeResultSet<T extends TreeRecord> {

  DatabaseTreeConfiguration configuration();

  T fromResultSet(ResultSet rs) throws SQLException;

  default ResultSetExtractor<List<TreeRecord>> resultSetExtractorList() {
    return rs -> {
      List<TreeRecord> nodes = new ArrayList<>();
      List<TreeRecord> trees = new ArrayList<>();
      Map<Integer, Set<TreeRecord>> treeListPartitions = new HashMap<>();
      Map<Integer, Optional<Integer>> nodeToParentMap = new HashMap<>();

      // TODO: pull this out into a common method
      while (rs.next()) {
        nodes.add(fromResultSet(rs));
      }

      if (nodes.isEmpty()) {
        return List.of();
      }

      for (TreeRecord node : nodes) {
        nodeToParentMap.put(node.id(), node.parent());
      }

      Integer currentRoot = 0;
      for (TreeRecord node : nodes) {
        if (node.parent().isEmpty()) {
          currentRoot = node.id();
          treeListPartitions.put(currentRoot, new HashSet<>(Set.of(node)));
        }

        if (findRootId(node.id(), nodeToParentMap).equals(currentRoot)) {
          Set<TreeRecord> descendents = treeListPartitions.get(currentRoot);
          descendents.add(node);
          treeListPartitions.put(currentRoot, descendents);
        }

      }

      for (Map.Entry<Integer, Set<TreeRecord>> partition : treeListPartitions.entrySet()) {
        trees.add(createTree(partition.getValue()));
      }

      return trees;
    };
  }

  default ResultSetExtractor<Optional<TreeRecord>> resultSetExtractor() {
    return rs -> {
      List<TreeRecord> nodes = new ArrayList<>();

      while (rs.next()) {
        nodes.add(fromResultSet(rs));
      }

      if (nodes.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(createTree(nodes));
    };
  }

}