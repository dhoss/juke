package in.stonecolddev.juke.blog;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BlogThread implements Thread<BlogPost> {

  private final DatabasePersistence databasePersistence;

  public BlogThread(DatabasePersistence databasePersistence) {
    this.databasePersistence = databasePersistence;
  }

  @Override
  public BlogPost newThread(BlogPost post) {
    return databasePersistence.save(post);
  }

  @Override
  public BlogPost addReply(BlogPost parent, BlogPost reply) {
    return databasePersistence.save(parent.addReply(reply));
  }

  @Override
  public List<BlogPost> retrieveThread(BlogPost post) {
    return databasePersistence.list(
        Pager.builder()
            .forThread(Optional.of(post))
            .build());
  }

  @Override
  public void deletePost(BlogPost post) {
    databasePersistence.delete(post);
  }

  @Override
  public List<BlogPost> deleteThread(BlogPost post) {
    return databasePersistence.bulkDelete(post.replies());
  }

  @Override
  public void approveReply(BlogPost post) {
    databasePersistence.save(post.approved(true));
  }
}