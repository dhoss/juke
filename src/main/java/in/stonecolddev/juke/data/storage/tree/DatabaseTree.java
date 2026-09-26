package in.stonecolddev.juke.data.storage.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
@With
@AllArgsConstructor
@EqualsAndHashCode
public class DatabaseTree<T extends TreeRecord> implements TreeRecord {// extends TreeRecord> {

  @JsonProperty("root")
  private final DatabaseTree<T> root;

  @JsonProperty("children")
  private final List<DatabaseTree<T>> children;

  private final T wrapped;

  @Builder.Default
  private final Boolean isRoot = false;

  @Builder.Default
  private final TreeMap<String, DatabaseTree<T>> nodePaths = new TreeMap<>();

  @Getter(AccessLevel.NONE)
  private final List<DatabaseTree<T>> nodes;

  private final List<Integer> path;

  public DatabaseTree<T> addChild(DatabaseTree<T> child) {
    //  this.nodes.add(child);
    System.out.println("**** CHILD " + child);
    this.nodePaths.put(child.pathAsString(), child);
    return DatabaseTree.<T>builder()
        //    .nodes(this.nodes)
        .root(this.root)
        .path(child.path())
        .nodePaths(this.nodePaths)
        .child(child)
        .build();
  }

  // public DatabaseTree<T> root() {
  //   return isRoot ? this : Optional.ofNullable(root).orElseGet(() -> create(wrapped));
  // }

  public static class DatabaseTreeBuilder<T extends TreeRecord> {// extends TreeRecord> {

    public DatabaseTreeBuilder<T> child(DatabaseTree<T> child) {
      List<DatabaseTree<T>> currentChildren =
          new ArrayList<>(
              Optional.ofNullable(this.children)
                  .orElseGet(List::of));
      currentChildren.add(child);
      this.children(currentChildren);
      return this;
    }

    //   public DatabaseTree<T> newThread() {
    //     final DatabaseTree<T> t = this.build();

    //     t.nodes.forEach(p -> t.nodePaths().put(p.pathAsString(), p));

    //     final DatabaseTree<T> root = findRootNode(t.nodePaths);

    //     return t.withRoot(root)
    //         .withChildren(
    //             t.nodePaths()
    //                 .tailMap(root.pathAsString(), false)
    //                 .values()
    //                 .stream()
    //                 .toList());
    //   }

  }

  private static <T extends TreeRecord> DatabaseTree<T> findRootNode(final TreeMap<String, DatabaseTree<T>> treeMap) {
    return treeMap.firstEntry().getValue();
  }

  public static <T extends TreeRecord> DatabaseTree<T> createNode(final T node, final T root) {
    return createNode(node, false).withRoot(createNode(root, true));
  }

  public static <T extends TreeRecord> DatabaseTree<T> createNode(final T node, final Boolean isRoot) {
    return DatabaseTree.<T>builder()
        .isRoot(isRoot)
        .wrapped(node)
        .path(node.path())
        .build();
  }

  public static <T extends TreeRecord> DatabaseTree<T> createTree(final List<T> nodes) {

    final TreeMap<String, DatabaseTree<T>> nodePaths = new TreeMap<>();

    nodes.stream().map(p -> DatabaseTree.<T>builder().path(p.path()).build())
        .forEach(p -> nodePaths.put(
            p.pathAsString(), p));

    final DatabaseTree<T> treeRoot = findRootNode(nodePaths);

    final List<DatabaseTree<T>> directDescendents = treeRoot.children().stream()
        .filter(
            d -> new HashSet<>(d.path()).containsAll(treeRoot.path())).toList();

    System.out.println("*** DIRECT DESCENDENTS OF " + treeRoot);

    return DatabaseTree.<T>builder()
        .root(treeRoot)
        .children(
            new ArrayList<>(
                nodePaths.tailMap(treeRoot.pathAsString(), false)
                    .values()))
        .build();
  }

}