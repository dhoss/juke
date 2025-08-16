package in.stonecolddev.juke.blog;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlogThreadStorage implements ThreadStorage<BlogThread, BlogPost> {

  private final BlogThreadDatabasePersistence blogThreadDatabasePersistence;

  public BlogThreadStorage(BlogThreadDatabasePersistence blogThreadDatabasePersistence) {
    this.blogThreadDatabasePersistence = blogThreadDatabasePersistence;
  }

  @Override
  public BlogThread newThread(BlogThread thread) {
    return blogThreadDatabasePersistence.save(thread);
  }

  @Override
  public BlogThread addReply(BlogPost reply) {
    return blogThreadDatabasePersistence.addReply(reply);
  }

  @Override
  public Optional<BlogThread> retrieveThread(BlogThread thread) {
    return blogThreadDatabasePersistence.find(thread);
  }

  @Override
  public void deletePost(BlogPost post) {
    blogThreadDatabasePersistence.deletePost(post);
  }

  @Override
  public void deleteThread(BlogThread thread) {
    blogThreadDatabasePersistence.deleteThread(thread);
  }

  @Override
  public void approveReply(BlogPost post) {
    blogThreadDatabasePersistence.approveReply(post);
  }
}