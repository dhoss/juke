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

import java.util.Map;
import java.util.Set;

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


  @Test
  public void find() {


  }

}