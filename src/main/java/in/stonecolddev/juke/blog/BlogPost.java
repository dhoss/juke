package in.stonecolddev.juke.blog;

import in.stonecolddev.juke.ui.page.Author;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Builder
@Accessors(fluent = true)
@Data
public class BlogPost {
  private Integer id;
  private Optional<BlogPost> parent;
  private List<BlogPost> replies;
  private String slug;
  private Author author;
  private String body;
  private Boolean approved;
  private OffsetDateTime publishedOn;
  private OffsetDateTime updatedOn;
  private OffsetDateTime createdOn;
}