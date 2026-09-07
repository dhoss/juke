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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("it-test")
@Tag("it-test")
public class TreeStorageServiceTest extends AbstractDatabaseTest {

  private final OffsetDateTime now = OffsetDateTime.parse("2026-09-06 16:14:20 -0600", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")).atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  DatabaseTreeConfiguration databaseTreeConfig = DatabaseTreeConfigurationBuilder.builder()
      .idColumn("id")
      .treeTableAlias("p")
      .queryParameters(Map.of("author", "devin"))
      .anchorQueryColumnSet(Set.of("author", "title", "body", "approved", "created_on", "published_on", "parent"))
      .treeTable("page_trees")
//      .remainingAnchorQuery("left join table2 t2 on t2.id=b.other_id")
//      .recursiveQueryColumnList(List.of("column1", "column2"))
      .parentColumn("parent")
//      .remainingRecursiveQuery("left join other_table o on o.id=b.other_id")
      .remainingCteQueryColumnsSet(Set.of("author_id", "email"))
      .whereColumn("slug")
      .build();


  PageRecord root = PageRecordBuilder.builder()
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

  PageRecord firstChild = PageRecordBuilder.builder()
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

  PageRecord firstChildFirstChild = PageRecordBuilder.builder()
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
          root
          ,
          firstChild
          ,
          firstChildFirstChild
      )
  );


  @Test
  public void find() {

    TreeStorageService<PageRecord> ts = new TreeStorageService<>(
        databaseTreeConfig,
        jdbcTemplate,
        rs -> {
          List<PageRecord> posts = new ArrayList<>();

          while (rs.next()) {
            posts.add(buildPageRecord(rs));
          }

          if (posts.isEmpty()) {
            return Optional.empty();
          }

          return Optional.of(DatabaseTree.create(posts));
        }
    );

    assertEquals(Optional.of(DatabaseTree.create(expectedNodes)), ts.find("test-root-page"));

  }

  // TODO: move this to PageRecord
  private PageRecord buildPageRecord(ResultSet rs) throws SQLException {
    PageRecordBuilder pageRecord = PageRecordBuilder.builder();
    pageRecord.id(rs.getInt(databaseTreeConfig.idColumn()));
    pageRecord.slug(rs.getString(databaseTreeConfig.whereColumn()));
    pageRecord.title(rs.getString("title"));
    pageRecord.body(rs.getString("body"));
    pageRecord.author(1);
    pageRecord.path(
        new ArrayList<>(
            Arrays.asList(
                (Integer[]) rs.getArray("path")
                    .getArray())));
    pageRecord.depth(rs.getInt("depth"));
    pageRecord.approved(rs.getBoolean("approved"));
    pageRecord.createdOn(
        rs.getObject("created_on", OffsetDateTime.class)
            .truncatedTo(ChronoUnit.SECONDS));
    pageRecord.publishedOn(
        rs.getObject("published_on", OffsetDateTime.class)
            .truncatedTo(ChronoUnit.SECONDS));

    // rs.getInt(...) will return 0 if the column value is null so we have to do this
    int parentId = rs.getInt("parent");
    if (parentId != 0) {
      pageRecord.parent(Optional.of(parentId));
    }

    return pageRecord.build();
  }

}