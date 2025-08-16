package in.stonecolddev.juke.blog;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.TreeMap;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
@With
public class BlogThread {

  private Integer blog;

  // TODO: implement this
  // if you change the root, it will re-parent the post
  private final BlogPost root;

  @Singular
  private final List<BlogPost> children;

  @Builder.Default
  private final TreeMap<String, BlogPost> postPaths = new TreeMap<>();

  @Getter(AccessLevel.NONE)
  private final List<BlogPost> posts;

  public BlogThread addReply(BlogPost reply) {
    return this.toBuilder().child(reply).build();
  }

  public static class BlogThreadBuilder {

    public BlogThread newThread() {
      final BlogThread t = this.build();

      t.posts.forEach(p -> t.postPaths().put(p.pathAsString(), p));

      final BlogPost root = findRootPost(t.postPaths);

      return t.withRoot(root)
          .withChildren(
              t.postPaths()
                  .tailMap(root.pathAsString(), false)
                  .values()
                  .stream()
                  .toList());
    }
  }

  private static BlogPost findRootPost(final TreeMap<String, BlogPost> threadMap) {
    return threadMap.firstEntry().getValue();
  }

  public static BlogThread create(final List<BlogPost> posts) {

    final TreeMap<String, BlogPost> postPaths = new TreeMap<>();

    posts.forEach(p -> postPaths.put(p.pathAsString(), p));

    final BlogPost threadRoot = findRootPost(postPaths);

    return BlogThread.builder()
        .blog(threadRoot.blog())
        .root(threadRoot)
        .children(postPaths.tailMap(threadRoot.pathAsString(), false).values())
        .build();
  }

}