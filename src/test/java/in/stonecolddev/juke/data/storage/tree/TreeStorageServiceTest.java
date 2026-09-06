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
import java.time.temporal.ChronoUnit;
import java.util.*;

import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("it-test")
@Tag("it-test")
public class TreeStorageServiceTest extends AbstractDatabaseTest {


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
      .anchorQueryColumnList(List.of("author", "title"))
      .treeTable("page_trees")
//      .remainingAnchorQuery("left join table2 t2 on t2.id=b.other_id")
//      .recursiveQueryColumnList(List.of("column1", "column2"))
      .parentColumn("parent")
//      .remainingRecursiveQuery("left join other_table o on o.id=b.other_id")
      .remainingCteQueryColumnsList(List.of("author_id", "email"))
      .whereColumn("slug")
      .build();

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

    ts.find("test");

  }

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
    pageRecord.publishedOn(
        rs.getObject("published_on", OffsetDateTime.class)
            .truncatedTo(ChronoUnit.SECONDS));

    // rs.getInt(...) will return 0 if the column value is null so we have to do this
    (rs.getInt("parent") == 0 ?
        Optional.empty() :
        Optional.of(rs.getInt("parent"))).ifPresent(
        pid -> pageRecord.parent(
            Optional.of(PageRecordBuilder.builder().id((Integer) pid).build())));

    return pageRecord.build();
  }

}