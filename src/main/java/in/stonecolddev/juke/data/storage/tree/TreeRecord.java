package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TreeRecord {

  String tableName();

  Optional<String> tableAlias();

  Set<String> columnList();

  String whereClause();

  Map<String, ?> valueMap();

  Integer id();

  String slug();

  Optional<Integer> parent();

  Set<TreeRecord> children();

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