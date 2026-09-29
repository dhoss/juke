package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TreeRecord {

  Integer id();

  Optional<Integer> parent();

  Set<TreeRecord> children();

  List<Integer> path();

  void addChild(TreeRecord child);

  default String pathAsString() {

    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }
}