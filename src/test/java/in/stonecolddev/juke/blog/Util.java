package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.Diff;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.DiffBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.DiffResult;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringStyle;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static in.stonecolddev.juke.blog.Helper.buildBlogPost;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Util extends AbstractDatabaseTest {


  public static BlogThread findThread(String slug) throws SQLException {

    List<BlogPost> posts = new ArrayList<>();
    try (ResultSet rs = performReadQuery(
        """
            with recursive cte as (
                          select
                              b.id as "post_id"
                            , b.blog
                            , b.title
                            , b.slug
                            , b.body
                            , b.published_on
                            , b.created_on
                            , b.parent
                            , b.approved
                            , a.user_name as "author"
                            , a.id as "author_id"
                            , a.email as "author_email"
                            , array[b.id] as "path"
                            , 1 as "depth"
                          from blog_posts b
                          left join authors a on a.id = b.author
                          where b.slug = '%s'
                          and b.approved = true
                          and b.published_on <= now()
            
                          union all
            
                          select
                              b.id as post_id
                            , b.blog
                            , b.title
                            , b.slug
                            , b.body
                            , b.published_on
                            , b.created_on
                            , b.parent
                            , b.approved
                            , a.user_name as "author"
                            , a.id as "author_id"
                            , a.email as "author_email"
                            , cte."path"  || b.id
                            , cte.depth + 1 as depth
                          from blog_posts b
                          join cte on b.parent = cte.post_id
                          left join authors a on b.author = a.id
                        )
                        select
                            post_id
                          , blog
                          , title
                          , slug
                          , body
                          , published_on
                          , created_on
                          , parent
                          , approved
                          , author
                          , author_id
                          , author_email
                          , path
                          , depth from cte
                        order by path;
            """.formatted(slug))) {

      while (rs.next()) {
        posts.add(buildBlogPost(rs));
      }
    }

    return BlogThread.create(posts);
  }

  public static void checkThread(BlogThread expected, BlogThread actual) throws SQLException {
    List<Diff<?>> postDiffs = compare(findThread(expected.root().slug()).root(), actual.root()).getDiffs();
    assertEquals(postDiffs.stream().map(Pair::getLeft).toList(), postDiffs.stream().map(Pair::getRight).toList());
  }

  public static void checkThreadWithChildren(BlogThread blogThread, List<BlogPost> blogPosts) {
    List<Diff<?>> postDiffs = compare(Fixtures.blogPost, blogThread.root()).getDiffs();
    assertEquals(postDiffs.stream().map(Pair::getLeft).toList(), postDiffs.stream().map(Pair::getRight).toList());

    assertEquals(Fixtures.sortedReplies(blogPosts), blogThread.children());
  }

  public static List<Diff<?>> compare(List<BlogPost> left, List<BlogPost> right) {
    List<Diff<?>> diffs = new ArrayList<>();

    for (var l : left) {
      for (var r: right) {
        diffs.addAll(compare(l, r).getDiffs());
      }
    }

    return diffs;
  }

  public static DiffResult<BlogPost> compare(BlogPost left, BlogPost right) {
    DiffBuilder<BlogPost> diffBuilder = new DiffBuilder<>(left, right, ToStringStyle.DEFAULT_STYLE);

    diffBuilder.append("blog", left.blog(), right.blog());
    diffBuilder.append("parent", left.parent(), right.parent());
    diffBuilder.append("author", left.author(), right.author());
    diffBuilder.append("body", left.body(), right.body());
    diffBuilder.append("publishedOn", left.publishedOn(), right.publishedOn());
    diffBuilder.append("path", left.path(), right.path());
    diffBuilder.append("depth", left.depth(), right.depth());
    diffBuilder.append("slug", left.slug(), right.slug());


    return diffBuilder.build();
  }

  public static class Fixtures {

    public static OffsetDateTime dateTime =
        OffsetDateTime.parse("2025-08-29T15:32:19Z").truncatedTo(ChronoUnit.SECONDS);

    public static Author author = Author.create("Devin").withId(1).withEmail("devin.austin@gmail.com");

    public static BlogPost blogPost = BlogPost.builder()
        .id(1)
        .blog(1)
        .path(List.of(1))
        .depth(1)
        .parent(Optional.empty())
        .title("test post")
        .slug("test-post")
        .author(author)
        .body("test post body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static BlogPost replyOneLevelOne = BlogPost.builder()
        .id(2)
        .blog(1)
        .depth(2)
        .path(List.of(1, 2))
        .parent(Optional.of(BlogPost.builder().id(blogPost.id()).build()))
        .title(blogPost.title() + " reply")
        .slug("test-post-reply")
        .author(author)
        .body("test post reply body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static BlogPost replyOneLevelTwo = BlogPost.builder()
        .id(3)
        .blog(1)
        .depth(3)
        .path(List.of(1, 2, 3))
        .parent(Optional.of(BlogPost.builder().id(replyOneLevelOne.id()).build()))
        .title("test post reply to reply")
        .slug("test-post-reply-to-reply")
        .author(author)
        .body("test post reply to reply body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static BlogPost replyTwoLevelOne = BlogPost.builder()
        .id(4)
        .blog(1)
        .depth(2)
        .path(List.of(1, 4))
        .parent(Optional.of(BlogPost.builder().id(blogPost.id()).build()))
        .title(blogPost.title() + " reply 2")
        .slug("test-post-reply-2")
        .author(author)
        .body("test post reply to reply body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static BlogPost replyTwoLevelTwo = BlogPost.builder()
        .id(5)
        .blog(1)
        .depth(3)
        .path(List.of(1, 2, 5))
        .parent(Optional.of(BlogPost.builder().id(replyOneLevelOne.id()).build()))
        .title(replyTwoLevelOne.title() + "reply 2")
        .slug("test-post-reply-to-reply")
        .author(author)
        .body("test post reply to reply body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static BlogPost replyOneLevelThree = BlogPost.builder()
        .id(6)
        .blog(1)
        .depth(4)
        .path(List.of(1, 2, 5, 6))
        .parent(Optional.of(BlogPost.builder().id(replyTwoLevelTwo.id()).build()))
        .title(replyTwoLevelTwo.title() + "reply 1")
        .slug("test-post-reply-to-reply")
        .author(author)
        .body("test post reply to reply body")
        .publishedOn(dateTime)
        .approved(true)
        .build();

    public static List<BlogPost> blogPosts =
        List.of(
            blogPost,
            replyOneLevelOne,
            replyTwoLevelOne,
            replyOneLevelTwo,
            replyTwoLevelTwo,
            replyOneLevelThree);

    public static BlogThread testThread() {
      return testThread(blogPosts);
    }

    public static BlogThread testThread(List<BlogPost> posts) {
      return BlogThread.create(posts);
    }

    public static BlogThread generateUniqueThread() {
      return testThread()
          .withBlog(1)
          .withRoot(
              blogPost.withId(4)
                  .withTitle("test save thread" + randomString())
                  .withSlug("test-save-thread" + randomString())
                  .withAuthor(author)
                  .withPath(List.of(6))
                  .publishedOn(dateTime)
                  .withApproved(true))
          .withChildren(List.of());

    }

    private static String randomString() {
      return RandomStringUtils.random(5);
    }

    public static BlogPost newReply(BlogPost parent) {
      return BlogPost.builder()
          .blog(parent.blog())
          .parent(Optional.of(parent))
          .slug(parent.slug() + "-reply")
          .author(author)
          .body(parent.body() + "reply")
          .publishedOn(dateTime)
          .approved(false)
          .build();
    }

    public static List<BlogPost> sortedReplies(List<BlogPost> blogPosts) {
      return blogPosts.stream()
          .filter(p -> !p.pathAsString().equals(blogPost.pathAsString()))
          .sorted(Comparator.comparing(BlogPost::pathAsString))
          .toList();
    }

    public static List<BlogPost> expectedDatabasePosts() {
      return blogPosts.stream()
          .filter(p ->
              p.id().equals(1) ||
              p.id().equals(2) ||
              p.id().equals(3))
          .toList();
    }


  }
}