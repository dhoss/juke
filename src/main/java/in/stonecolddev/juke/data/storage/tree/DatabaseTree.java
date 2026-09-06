package in.stonecolddev.juke.data.storage.tree;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
@With
public class DatabaseTree<T extends TreeRecord> {

  private Integer author;

  // TODO: implement this
  // if you change the root, it will re-parent the post
  private final T root;

  private final List<T> children;

  @Builder.Default
  private final TreeMap<String, T> nodePaths = new TreeMap<>();

  @Getter(AccessLevel.NONE)
  private final List<T> nodes;

  public DatabaseTree<T> addChild(T child) {
    return DatabaseTree.<T>builder()
        .nodes(this.nodes)
        .root(this.root)
        .nodePaths(this.nodePaths)
        .child(child)
        .build();
  }

  public static class DatabaseTreeBuilder<T extends TreeRecord> {

    public DatabaseTreeBuilder<T> child(T child) {
      List<T> currentChildren = new ArrayList<>(this.children);
      currentChildren.add(child);
      this.children(currentChildren);
      return this;
    }

    public DatabaseTree<T> newThread() {
      final DatabaseTree<T> t = this.build();

      t.nodes.forEach(p -> t.nodePaths().put(p.pathAsString(), p));

      final T root = findRootNode(t.nodePaths);

      return t.withRoot(root)
          .withChildren(
              t.nodePaths()
                  .tailMap(root.pathAsString(), false)
                  .values()
                  .stream()
                  .toList());
    }

  }

  private static <T> T findRootNode(final TreeMap<String, T> treeMap) {
    return treeMap.firstEntry().getValue();
  }

  public static <T extends TreeRecord> DatabaseTree<T> create(final List<T> nodes) {

    final TreeMap<String, T> nodePaths = new TreeMap<>();

    nodes.forEach(p -> nodePaths.put(p.pathAsString(), p));

    final T treeRoot = findRootNode(nodePaths);

    return DatabaseTree.<T>builder()
        .root(treeRoot)
        .children(
            new ArrayList<>(
                nodePaths.tailMap(treeRoot.pathAsString(), false)
                    .values()))
        .build();
  }

}