package in.stonecolddev.juke.data.storage.tree;

import java.util.*;

public class DatabaseTree {

  public static Integer findRootId(
      Integer nodeId, Map<Integer, Optional<Integer>> nodeToParentMap) {
    Optional<Integer> maybeNodeParentId = nodeToParentMap.get(nodeId);
    if (maybeNodeParentId.isEmpty())
      return nodeId;

    Integer lastCheckedId = maybeNodeParentId.get();
    while (true) {
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
    // find root (parent is undef)
    // if no node's parent is undef, find the lowest id and make it parent
    // not sure if that's ideal
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parent().isEmpty())
            .findFirst()
            .orElseGet(
                () ->
                    nodes.stream()
                        .min(Comparator.comparing(TreeRecord::id))
                        .orElseThrow(() -> new RuntimeException("No tree root in createTree")));

    for (TreeRecord node : nodes) {

      if (node.parent().isPresent() && !node.id().equals(root.id())) {
        nodes.stream()
            .filter(
                parentNode -> parentNode.id().equals(
                    node.parent().orElseThrow(
                        () -> new RuntimeException("Parent of node doesn't exist but we really shouldn't get here"))))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No such parent for node"))
            .addChild(node);
      }
    }

    return root;
  }
}