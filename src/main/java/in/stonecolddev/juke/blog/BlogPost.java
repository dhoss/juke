package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.With;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@Data
@With
public class BlogPost {
  private Integer id;

  @Builder.Default
  private Optional<BlogPost> parent = Optional.empty();

  @Singular("addReply")
  private final List<BlogPost> replies;
  private String slug;
  private Author author;
  private String body;
  private Boolean approved;
  private OffsetDateTime publishedOn;
  private OffsetDateTime updatedOn;
  private OffsetDateTime createdOn;

  public BlogPost addReply(BlogPost reply) {
    return this.toBuilder().addReply(reply).build();
  }

}