package in.stonecolddev.juke.data.storage.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static in.stonecolddev.juke.data.storage.tree.TreeFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTreeTest {

  @Test
  public void createTree() {
    assertEquals(
        fullTree,
        DatabaseTree.createTree(
            List.of(
                root,
                firstChild,
                firstChildFirstChild,
                firstChildSecondChild)));
  }
}