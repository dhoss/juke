package in.stonecolddev.juke.data.storage.tree;

import java.util.*;

public class DatabaseTree {

  // TODO: memoize this
  public static Integer findRootId(
      Integer nodeId, Map<Integer, Optional<Integer>> nodeToParentMap) {
    Optional<Integer> maybeNodeParentId = nodeToParentMap.get(nodeId);
    //Optional<Integer> maybeNodeParentId = Optional.ofNullable(nodeToParentMap.get(nodeId)).flatMap(p -> p);
    if (maybeNodeParentId.isEmpty())
      return nodeId;

    System.out.println("****NODE ID " + nodeId);
    System.out.println("**** NODETOPARENTMAP " + nodeToParentMap);
    System.out.println("**** MAYBENODEPARENTID " + maybeNodeParentId);
    Integer lastCheckedId = maybeNodeParentId.get();
    System.out.println("**** LASTCHECKEDID " + lastCheckedId);
    while (true) {

      System.out.println("**** LASTCHECKEDID IN LOOP " + lastCheckedId);
      System.out.println("**** MAYBENODEPARENTID IN LOOP " + maybeNodeParentId);
      if (Optional.ofNullable(maybeNodeParentId).isEmpty())
        throw new RuntimeException("maybeNodeParentId is null, something's broken!");
      if (maybeNodeParentId.isPresent()) {
        lastCheckedId = maybeNodeParentId.get();
        maybeNodeParentId = nodeToParentMap.get(lastCheckedId);
      } else {
        return lastCheckedId;
      }
    }
  }

  public static TreeRecord createTree(Set<TreeRecord> nodes) {
    return createTree(nodes.stream().toList());
  }

  // TODO: clean this up
  // TODO: this should take a Set
  public static TreeRecord createTree(List<TreeRecord> nodes) {
    // TODO: use "lowest" path instead of id since id may not be sequential
    // find root (parentId is undef)
    // if no node's parentId is undef, find the lowest id and make it parentId
    // not sure if that's ideal
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parentId().isEmpty())
            .findFirst()
            .orElseGet(
                () ->
                    nodes.stream()
                        .min(Comparator.comparing(TreeRecord::id))
                        .orElseThrow(() -> new RuntimeException("No tree root in createTree")));

    for (TreeRecord node : nodes) {

      if (node.parentId().isPresent() && !node.id().equals(root.id())) {
        nodes.stream()
            .filter(
                parentNode -> parentNode.id().equals(
                    node.parentId().orElseThrow(
                        () -> new RuntimeException("Parent of node doesn't exist but we really shouldn't get here"))))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No such parentId for node"))
            .addChild(node);
      }
    }

    return root;
  }
}