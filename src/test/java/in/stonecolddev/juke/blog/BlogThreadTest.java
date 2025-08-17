package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BlogThreadTest {

  private final DatabasePersistence persistence = mock(DatabasePersistence.class);

  private final BlogThread blogThread = new BlogThread(persistence);

  private final Author author = Author.builder().userName("devin").build();
  private final BlogPost rootBlogPost =
      BlogPost.builder()
          .id(1)
          .approved(true)
          .author(author)
          .body("Test post")
          .createdOn(OffsetDateTime.now())
          .publishedOn(OffsetDateTime.now())
          .build();

  private final BlogPost reply =
      BlogPost.builder()
          .id(2)
          .approved(false)
          .parent(Optional.of(rootBlogPost))
          .author(author)
          .createdOn(OffsetDateTime.now())
          .build();

  private final BlogPost reply2 =
      BlogPost.builder()
          .id(3)
          .approved(false)
          .parent(Optional.of(rootBlogPost))
          .author(author)
          .createdOn(OffsetDateTime.now())
          .build();

  private final BlogPost postWithReply = rootBlogPost.withReplies(List.of(reply));

  @Test
  public void newParentThread() {
    when(persistence.save(rootBlogPost)).thenReturn(rootBlogPost);
    assertEquals(rootBlogPost, blogThread.newThread(rootBlogPost));
  }

  @Test
  public void addReply() {
    when(persistence.save(postWithReply)).thenReturn(postWithReply);
    assertEquals(postWithReply, blogThread.addReply(rootBlogPost, reply));
  }

  @Test
  public void retrieveThread() {
    when(
        persistence.list(
            Pager.builder()
                .forThread(Optional.of(rootBlogPost))
                .build()))
        .thenReturn(List.of(reply, reply2));

    assertEquals(List.of(reply, reply2), blogThread.retrieveThread(rootBlogPost));
  }

  @Test
  public void deletePost() {
    blogThread.deletePost(rootBlogPost);
    verify(persistence, times(1)).delete(rootBlogPost);
  }

  @Test
  public void deleteThread() {
    blogThread.deleteThread(rootBlogPost);
    verify(persistence, times(1)).bulkDelete(rootBlogPost.replies());
  }

  @Test
  public void approveReply() {
    blogThread.approveReply(rootBlogPost);
    verify(persistence, times(1)).save(rootBlogPost.approved(true));
  }
}