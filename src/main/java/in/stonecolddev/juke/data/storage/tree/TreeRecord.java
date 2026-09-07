package in.stonecolddev.juke.data.storage.tree;

import java.util.List;
import java.util.stream.Collectors;

public interface TreeRecord {
  List<Integer> path();

  default String pathAsString() {

    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }
}