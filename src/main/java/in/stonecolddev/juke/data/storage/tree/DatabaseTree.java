package in.stonecolddev.juke.data.storage.tree;

import java.util.List;

public class DatabaseTree {

  public static TreeRecord createTree(List<TreeRecord> nodes) {
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parent().isEmpty())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No root node defined in tree"));

    for (TreeRecord node : nodes) {

      if (node.parent().isPresent()) {
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