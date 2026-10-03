package in.stonecolddev.juke.data.storage.tree;

import java.util.Comparator;
import java.util.List;

public class DatabaseTree {

  public static TreeRecord createTree(List<TreeRecord> nodes) {
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parent().isEmpty())
            .findFirst()
            .orElseGet(
                () ->
                    nodes.stream()
                        .peek((c -> {
                          System.out.println("**** TREE RECORD ID IN CREATETREE " + c.id() + ", PARENT " + c.parent());
                        }))
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