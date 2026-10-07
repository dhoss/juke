package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TreeRecord {

  // TODO: everything not directly related to a tree
  //       tableName, tableAlias, etc
  //       should be migrated to a more generic Record interface
  String tableName();

  Optional<String> tableAlias();

  Set<String> columnList();

  String whereClause();

  // TODO: rename this to something more clear
  //       it's for holding k/v pairs to pass in to MapSqlParameterSource
  Map<String, ?> valueMap();

  String primaryKey();

  Integer id();

  String slug();

  Optional<Integer> parent();

  TreeRecord reparent(TreeRecord parent);

  Set<TreeRecord> children();

  Set<Integer> ancestors();

  List<Integer> path();

  void addChild(TreeRecord child);

  // TODO: repurpose this to build the tree path for the URL
  default String pathAsString() {

    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }
}