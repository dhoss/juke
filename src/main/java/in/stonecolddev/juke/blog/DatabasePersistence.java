package in.stonecolddev.juke.blog;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabasePersistence implements Persistence<BlogPost> {
  @Override
  public BlogPost save(BlogPost post) {
    return null;
  }

  @Override
  public BlogPost find(BlogPost post) {
    return null;
  }

  @Override
  public List<BlogPost> list(Pager page) {
    return List.of();
  }

  @Override
  public void delete(BlogPost post) {

  }

  @Override
  public List<BlogPost> bulkDelete(List<BlogPost> posts) {
    return List.of();
  }
}