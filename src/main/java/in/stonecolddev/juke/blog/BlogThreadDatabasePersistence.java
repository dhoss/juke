package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static in.stonecolddev.juke.blog.Helper.buildBlogPost;

@Component
public class BlogThreadDatabasePersistence implements Persistence<BlogThread> {

  // TODO: make these a bundled component
  private final Logger log = LoggerFactory.getLogger(BlogThreadDatabasePersistence.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public BlogThreadDatabasePersistence(
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public BlogThread save(BlogThread thread) {
    perRequestMetricsCollector.incrementPageQueryCounter();

    BlogPost root = thread.root();
    String slug = root.slug();

    if (namedParameterJdbcTemplate.update(
        """
            insert into blog_posts(blog, author, title, slug, body, approved, published_on, created_on)
            values(:blog, :authorId, :title, :slug, :body, true, :publishedOn, now())
            """,
        new MapSqlParameterSource().addValues(
            Map.of(
                "blog", thread.blog(),
                "title", root.title(),
                "slug", slug,
                "body", root.body(),
                "publishedOn", root.publishedOn(),
                "authorId", root.author().id()))) < 1) {
      throw new RuntimeException("Insert failed for thread " + thread.root().slug());
    }

    return find(
        BlogThread.builder()
            .root(
                BlogPost.builder()
                    .slug(slug)
                    .build())
            .build())
        .orElseThrow(() -> new RuntimeException("Can't find thread by slug " + slug));
  }

  @Override
  public Optional<BlogThread> find(BlogThread blogThread) {

    perRequestMetricsCollector.incrementPageQueryCounter();

    return namedParameterJdbcTemplate.query(
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
                          where b.slug = :slug
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
                """,
        new MapSqlParameterSource().addValue("slug", blogThread.root().slug()),
        rs -> {

              List<BlogPost> posts = new ArrayList<>();

              while (rs.next()) {
                posts.add(buildBlogPost(rs));
              }

              if (posts.isEmpty()) {
                log.info("No post found for slug {}", blogThread.root().slug());
                return Optional.empty();
              }

              return Optional.of(BlogThread.create(posts));
        }
    );
  }


  @Override
  public List<BlogThread> list(Pager page) {
    return List.of();
  }

  public void deletePost(BlogPost post) {

  }

  public BlogThread addReply(BlogPost reply) {
    perRequestMetricsCollector.incrementPageQueryCounter();

    BlogPost parent = reply.parent()
        .orElseThrow(() -> new RuntimeException("Reply does not have a parent: " + reply.slug()));
    String parentSlug = parent.slug();
    String slug = parentSlug + "-reply";

    if (namedParameterJdbcTemplate.update(
        """
            insert into blog_posts(
                blog
              , author
              , parent
              , title
              , slug
              , body
              , approved
              , published_on
              , created_on)
            values(
                :blog
              , :authorId
              , :parentId
              , :title
              , :slug
              , :body
              , :approved
              , :publishedOn
              , now())
            """,
        new MapSqlParameterSource().addValues(
            Map.of(
                "blog", parent.blog(),
                "authorId", reply.author().id(),
                "parentId", parent.id(),
                "title", "RE: " +parent.title(),
                "slug", slug,
                "body", reply.body(),
                "approved", reply.approved(),
                "publishedOn", reply.publishedOn()))) < 1) {
      throw new RuntimeException("Insert failed for reply " + reply.slug());
    }

    return find(
        // TODO: create method to build this
        BlogThread.builder()
            .root(
                BlogPost.builder()
                    .slug(parentSlug)
                    .build())
            .build())
        .orElseThrow(() -> new RuntimeException("Can't find thread by slug " + parentSlug));
  }

  public void approveReply(BlogPost post) {

  }

  @Override
  public void deleteThread(BlogThread thread) {
  }

  @Override
  public void bulkDeleteThreads(List<BlogThread> threads) {
  }
}