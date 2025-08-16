package in.stonecolddev.juke.blog;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static in.stonecolddev.juke.blog.Util.Fixtures.replyOneLevelOne;
import static in.stonecolddev.juke.blog.Util.Fixtures.testThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BlogThreadStorageTest {

  private final BlogThreadDatabasePersistence persistence = mock(BlogThreadDatabasePersistence.class);

  private final BlogThreadStorage blogThreadStorage = new BlogThreadStorage(persistence);

  private final BlogThread blogThread = testThread();

  @Test
  public void newThread() {
    when(persistence.save(blogThread)).thenReturn(blogThread);
    assertEquals(blogThread, blogThreadStorage.newThread(blogThread));
    verify(persistence, times(1)).save(blogThread);
  }

  @Test
  public void addReply() {
    when(persistence.addReply(replyOneLevelOne)).thenReturn(blogThread); // TODO: return actual updated thread
    assertEquals(blogThread, blogThreadStorage.addReply(replyOneLevelOne));
    verify(persistence, times(1)).addReply(replyOneLevelOne);
  }

  @Test
  public void retrieveThread() {
    Optional<BlogThread> maybeBlogThread = Optional.of(blogThread);
    when(persistence.find(blogThread)).thenReturn(maybeBlogThread);
    assertEquals(maybeBlogThread, blogThreadStorage.retrieveThread(blogThread));
    verify(persistence, times(1)).find(blogThread);
  }

  @Test
  public void deletePost() {
    BlogPost root = blogThread.root();
    blogThreadStorage.deletePost(root);
    verify(persistence, times(1)).deletePost(root);
  }

  @Test
  public void deleteThread() {
    blogThreadStorage.deleteThread(blogThread);
    verify(persistence, times(1)).deleteThread(blogThread);
  }

  @Test
  public void approveReply() {
    BlogPost replyToApprove = blogThread.children().getFirst().withApproved(true);
    blogThreadStorage.approveReply(replyToApprove);
    verify(persistence, times(1)).approveReply(replyToApprove);
  }
}