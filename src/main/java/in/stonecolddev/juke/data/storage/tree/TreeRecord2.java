package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TreeRecord2<T> {


  Optional<Integer> parent();

  Set<T> children();

  List<Integer> path();

  default String pathAsString() {

    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }
}