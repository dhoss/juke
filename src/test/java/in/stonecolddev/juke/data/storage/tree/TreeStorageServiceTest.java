package in.stonecolddev.juke.data.storage.tree;

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
import java.util.*;

import static in.stonecolddev.juke.data.storage.tree.PageRecord.fromResultSet;
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

  List<PageRecord> expectedNodes = new ArrayList<>(
      List.of(
          root,
          firstChild,
          firstChildFirstChild
      )
  );


  @Test
  public void find() {

    TreeStorageService<PageRecord> ts = new TreeStorageService<>(
        databaseTreeConfig,
        jdbcTemplate,
        rs -> {
          // TODO: could this be generalized?
          List<PageRecord> posts = new ArrayList<>();

          while (rs.next()) {
            posts.add(fromResultSet(rs, databaseTreeConfig));
          }

          if (posts.isEmpty()) {
            return Optional.empty();
          }

          return Optional.of(DatabaseTree.create(posts));
        }
    );

    assertEquals(
        Optional.of(DatabaseTree.create(expectedNodes)),
        ts.find("test-root-page"));

  }
}