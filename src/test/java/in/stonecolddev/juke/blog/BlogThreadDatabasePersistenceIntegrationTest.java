package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.Optional;

import static in.stonecolddev.juke.blog.Util.Fixtures.*;
import static in.stonecolddev.juke.blog.Util.checkThread;
import static in.stonecolddev.juke.blog.Util.checkThreadWithChildren;
import static in.stonecolddev.juke.util.Fixtures.Database.startDatabase;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("it-test")
@Tag("it-test")
public class BlogThreadDatabasePersistenceIntegrationTest extends AbstractDatabaseTest {

  @Autowired
  private BlogThreadDatabasePersistence blogThreadDatabasePersistence;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  @Test
  public void findThread() {
    checkThreadWithChildren(
        blogThreadDatabasePersistence.find(testThread()).orElseThrow(),
        expectedDatabasePosts());
  }

  @Test
  public void saveThread() throws SQLException {
    final BlogThread thread = generateUniqueThread();
    checkThread(
        thread,
        blogThreadDatabasePersistence.save(thread));
  }

  @Test
  public void addReply() throws SQLException {

    final BlogThread thread = generateUniqueThread();
    final BlogPost parent = thread.root();
    final BlogPost reply = newReply(parent).withApproved(true);

    checkThread(
        thread,
        blogThreadDatabasePersistence.save(thread));

    checkThread(
        thread.addReply(reply.withParent(Optional.of(parent))),
        blogThreadDatabasePersistence.addReply(reply));

  }

}