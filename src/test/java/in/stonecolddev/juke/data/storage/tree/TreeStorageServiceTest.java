package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.PageRecord;
import in.stonecolddev.juke.page.PageTreeResultSet;
import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static in.stonecolddev.juke.data.storage.tree.TreeFixtures.fullTree;
import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

  // TODO: each tree type should have its own DatabaseTreeConfiguration
  //       bean instance/type, otherwise different tree types are probably going to have issues
  //       on instantiation
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

  //@Autowired
  // TODO: I don't like having to pass in DatabaseTreeConfig to both PageTreeResultSet and TreeStorageService
  private final PageTreeResultSet pageTreeResultSet = new PageTreeResultSet(databaseTreeConfig);

  @Test
  public void find() {

    final TreeStorageService<PageRecord> ts =
        new TreeStorageService<>(databaseTreeConfig, jdbcTemplate, pageTreeResultSet);
    assertEquals(Optional.of(fullTree), ts.find("test-root-page"));

  }

}