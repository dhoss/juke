package in.stonecolddev.juke.data.storage.tree;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
@With
@AllArgsConstructor
@EqualsAndHashCode
public class DatabaseTree<T extends TreeRecord> {

  public static TreeRecord createTree(List<TreeRecord> nodes) {
    // TODO: consider caching these functional searches in a HashMap
    TreeRecord root =
        nodes.stream()
            .filter(node -> node.parent().isEmpty())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No root node defined in tree"));

    for (TreeRecord node : nodes) {
      System.out.println("**** CURRENT NODE " + node.id());
      System.out.println("**** CURRENT NODE PARENT " + node.parent());
      System.out.println("**** CURRENT NODE CHILDREN " + node.children());

      if (node.parent().isPresent()) {
        TreeRecord parent =
            nodes.stream()
                .filter(parentNode -> parentNode.id().equals(node.parent().get()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such parent for node"));
        parent.addChild(node);
      }
      System.out.println("**** ROOT AFTER UPDATE " + root.children());
    }
    return root;
  }
}