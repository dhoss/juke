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

import java.util.Optional;

import static in.stonecolddev.juke.data.storage.tree.TreeFixtures.fullTree;
import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@ActiveProfiles("it-test")
@Tag("it-test")
public class TreeStorageServiceTest extends AbstractDatabaseTest {

  @Autowired
  private TreeStorageService<PageRecord> ts;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  @Test
  public void find() {

    assertEquals(Optional.of(fullTree), ts.find("test-root-page"));

  }

}