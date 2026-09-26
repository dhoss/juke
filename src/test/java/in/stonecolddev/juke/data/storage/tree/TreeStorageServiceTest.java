package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.PageRecord;
import in.stonecolddev.juke.page.PageRecord2;
import in.stonecolddev.juke.page.PageRecord2Builder;
import in.stonecolddev.juke.page.PageRecordBuilder;
import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("it-test")
@Tag("it-test")
public class TreeStorageServiceTest extends AbstractDatabaseTest {

  private final OffsetDateTime now =
      OffsetDateTime.parse(
              "2026-09-06 16:14:20 -0600",
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
          .atZoneSameInstant(ZoneId.of("UTC"))
          .toOffsetDateTime();

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  private final DatabaseTreeConfiguration databaseTreeConfig =
      DatabaseTreeConfigurationBuilder.builder()
          .idColumn("id")
          .treeTableAlias("p")
          .queryParameters(Map.of("author", "devin"))
          .anchorQueryColumnSet(
              Set.of(
                  "author",
                  "title",
                  "body",
                  "approved",
                  "created_on",
                  "published_on",
                  "parent"))
          .treeTable("page_trees")
          .parentColumn("parent")
          .remainingCteQueryColumnsSet(Set.of("author_id", "email"))
          .whereColumn("slug")
          .build();


  private final PageRecord root = PageRecordBuilder.builder()
      .id(1)
      .author(1)
      .title("test root page")
      .slug("test-root-page")
      .body("test root page body")
      .path(List.of(1))
      .depth(1)
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  private final PageRecord firstChild = PageRecordBuilder.builder()
      .id(2)
      .author(1)
      .title("test root page first child page")
      .slug("test-root-page-first-child-page")
      .body("test root page first child page body")
      .path(List.of(1, 2))
      .depth(2)
      .parent(Optional.of(root.id()))
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  private final PageRecord firstChildFirstChild = PageRecordBuilder.builder()
      .id(3)
      .author(1)
      .title("test root page first child page first child")
      .slug("test-root-page-first-child-page-first-child")
      .body("test root page first child page body first child")
      .path(List.of(1, 2, 3))
      .depth(3)
      .parent(Optional.of(firstChild.id()))
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();


  DatabaseTree<PageRecord> expectedNodes =
      DatabaseTree.createNode(root, true)
          .addChild(
              DatabaseTree.createNode(firstChild, root)
                  .addChild(
                      DatabaseTree.createNode(firstChildFirstChild, firstChild)));


  @Test
  public void find() {

    final PageRecord2 root = PageRecord2Builder.builder()
        .id(1)
        .author(1)
        .title("test root page")
        .slug("test-root-page")
        .body("test root page body")
        .path(List.of(1))
        .depth(1)
        .approved(true)
        .createdOn(now)
        .publishedOn(now)
        .build();

    final PageRecord2 firstChild = PageRecord2Builder.builder()
        .id(2)
        .author(1)
        .title("test root page first child page")
        .slug("test-root-page-first-child-page")
        .body("test root page first child page body")
        .path(List.of(1, 2))
        .depth(2)
        .parent(Optional.of(root.id()))
        .approved(true)
        .createdOn(now)
        .publishedOn(now)
        .build();

    final PageRecord2 firstChildFirstChild = PageRecord2Builder.builder()
        .id(3)
        .author(1)
        .title("test root page first child page first child")
        .slug("test-root-page-first-child-page-first-child")
        .body("test root page first child page body first child")
        .path(List.of(1, 2, 3))
        .depth(3)
        .parent(Optional.of(firstChild.id()))
        .approved(true)
        .createdOn(now)
        .publishedOn(now)
        .build();

    final PageRecord2 firstChildSecondChild = PageRecord2Builder.builder()
        .id(4)
        .author(1)
        .title("test root page first child page second child")
        .slug("test-root-page-first-child-page-second-child")
        .body("test root page second child page body first child")
        .path(List.of(1, 2, 3))
        .depth(3)
        .parent(Optional.of(firstChild.id()))
        .approved(true)
        .createdOn(now)
        .publishedOn(now)
        .build();
    //   TreeStorageService<PageRecord> ts = new TreeStorageService<>(
    //       databaseTreeConfig,
    //       jdbcTemplate,
    //       PageTreeResultSetBuilder.builder().configuration(databaseTreeConfig).build()
    //   );

    //   assertEquals(
    //       Optional.of(expectedNodes),
    //       ts.find("test-root-page"));

    assertEquals(root.withChildren(Set.of(firstChild)), createTree(List.of(root, firstChild, firstChildFirstChild, firstChildSecondChild)));
    System.out.println("**** FULL TREE ");
    System.out.println("ROOT : " + root.id());
    for (PageRecord2 node : root.children()) {
      System.out.println("NODE: " + node.id() + " PARENT : " + node.parent());
      for (PageRecord2 subNode : node.children()) {
        System.out.println("SUBNODE " + subNode.id() + " SUBNODE PARENT " + subNode.parent());
      }
    }

  }

  private PageRecord2 createTree(List<PageRecord2> nodes) {
    PageRecord2 root = nodes.stream().filter(node -> node.parent().isEmpty()).findFirst().orElseThrow(() -> new RuntimeException("No root node defined in tree"));

    for (PageRecord2 node : nodes) {
      System.out.println("**** CURRENT NODE " + node.title() + " " + node.id());
      System.out.println("**** CURRENT NODE PARENT " + node.parent());
      System.out.println("**** CURRENT NODE CHILDREN " + node.children());

      if (node.parent().isPresent()) {
        PageRecord2 parent = nodes.stream().filter(parentNode -> parentNode.id().equals(node.parent().get())).findFirst().orElseThrow(() -> new RuntimeException("No such parent for node"));
        parent.addChild(node);
      }
      System.out.println("**** ROOT AFTER UPDATE " + root.children().stream().map(child -> child.id()).toList());
    }
    return root;
  }
}