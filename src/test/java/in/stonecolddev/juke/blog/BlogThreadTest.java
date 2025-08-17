package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlogThreadTest {

  private final DatabasePersistence persistence = mock(DatabasePersistence.class);

  private final BlogThread blogThread = new BlogThread(persistence);

  private final Author author = Author.builder().userName("devin").build();
  private final BlogPost expectedBlogPost = BlogPost.builder()
        .id(1)
        .approved(true)
        .author(author)
        .body("Test post")
        .createdOn(OffsetDateTime.now())
        .publishedOn(OffsetDateTime.now())
        .build();

  @Test
  public void newParentThread() {
    when(persistence.save(expectedBlogPost)).thenReturn(expectedBlogPost);

    assertEquals(expectedBlogPost, blogThread.newThread(expectedBlogPost));
  }

  @Test
  public void addReply() {
    BlogPost reply =
        BlogPost.builder()
            .id(2)
            .approved(false)
            .parent(Optional.of(expectedBlogPost))
            .author(author)
            .createdOn(OffsetDateTime.now())
            .build();

    BlogPost postWithReply = expectedBlogPost.withReplies(List.of(reply));
    when(persistence.save(postWithReply)).thenReturn(postWithReply);

    assertEquals(postWithReply, blogThread.addReply(expectedBlogPost, reply));
  }

}