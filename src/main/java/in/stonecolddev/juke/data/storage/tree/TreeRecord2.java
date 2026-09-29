package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TreeRecord2 {

  Integer id();

  Optional<Integer> parent();

  Set<TreeRecord2> children();

  List<Integer> path();

  void addChild(TreeRecord2 child);

  default String pathAsString() {

    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }
}