package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


@Slf4j
public class Helper {

  public static BlogPost buildBlogPost(ResultSet rs) throws SQLException {
    log.info("Building blog post");
    BlogPost.BlogPostBuilder blogPost = BlogPost.builder();
    blogPost.id(rs.getInt("post_id"));
    blogPost.blog(rs.getInt("blog"));
    blogPost.slug(rs.getString("slug"));
    blogPost.title(rs.getString("title"));
    blogPost.body(rs.getString("body"));
    blogPost.author(
        Author.builder()
            .userName((rs.getString("author")))
            .email(rs.getString("author_email"))
            .id(rs.getInt("author_id"))
            .build());
    blogPost.path(
        new ArrayList<>(
            Arrays.asList(
                (Integer[]) rs.getArray("path").getArray())));
    blogPost.depth(rs.getInt("depth"));
    blogPost.approved(rs.getBoolean("approved"));
    blogPost.publishedOn(
        rs.getObject("published_on", OffsetDateTime.class).truncatedTo(ChronoUnit.SECONDS));

    // rs.getInt(...) will return 0 if the column value is null so we have to do this
    (rs.getInt("parent") == 0 ?
        Optional.empty() :
        Optional.of(rs.getInt("parent"))).ifPresent(
        pid -> blogPost.parent(
            Optional.of(BlogPost.builder().id((Integer) pid).build())));

    return blogPost.build();
  }

}