package in.stonecolddev.juke.data.storage.tree;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static in.stonecolddev.juke.data.storage.tree.TreeFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTreeTest {

  @Test
  public void createTree() {
    assertEquals(root.withChildren(Set.of(firstChild)), DatabaseTree.createTree(
        List.of(
            root,
            firstChild,
            firstChildFirstChild,
            firstChildSecondChild)));
  }

}