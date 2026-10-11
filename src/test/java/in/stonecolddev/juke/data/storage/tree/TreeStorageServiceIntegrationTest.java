package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.PageRecord;
import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static in.stonecolddev.juke.data.storage.tree.TreeFixtures.*;
import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@ActiveProfiles("it-test")
@Tag("it-test")
public class TreeStorageServiceIntegrationTest extends AbstractDatabaseTest {

  @Autowired
  private TreeStorageService<PageRecord> treeStorageService;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  @Test
  public void find() {

    // TODO: unhappy path tests
    assertEquals(
        Optional.of(fullTree), treeStorageService.find(fullTree.slug()));

  }

  // TODO: implement test for listTrees
  @Test
  public void listTrees() {
  }

  @Test
  public void create() {
    PageRecord newTree = newTreeRoot();
    assertEquals(newTree, treeStorageService.create(newTree));
  }

  @Test
  public void addChild() {
    PageRecord newTree = newTreeRoot();
    newTree = (PageRecord) treeStorageService.create(newTree);
    PageRecord childNode = newChild(newTree);
    // TODO: add test in create() to create tree with children
    assertEquals(
        newTree.withChildren(new HashSet<>(Set.of(childNode))),
        treeStorageService.addChild(newTree, childNode));
  }
}