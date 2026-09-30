package in.stonecolddev.juke.data.storage.tree;

import java.util.List;

public class DatabaseTree {

  public static TreeRecord createTree(List<TreeRecord> nodes) {
    // TODO: consider caching these functional searches in a HashMap
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parent().isEmpty())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No root node defined in tree"));

    for (TreeRecord node : nodes) {

      if (node.parent().isPresent()) {
        nodes.stream()
            .filter(parentNode -> parentNode.id().equals(node.parent().get()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No such parent for node"))
            .addChild(node);
      }
    }

    return root;
  }
}