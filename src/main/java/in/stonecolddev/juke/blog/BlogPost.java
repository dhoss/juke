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
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@Data
@With
public class BlogPost {
  private Integer id;

  private Integer blog;

  @Builder.Default
  private Optional<BlogPost> parent = Optional.empty();

  @Singular("reply")
  private final List<BlogPost> replies;
  private String title;
  private String slug;
  private Author author;
  private String body;
  private Boolean approved;
  private OffsetDateTime publishedOn;
  private OffsetDateTime updatedOn;
  private OffsetDateTime createdOn;
  private List<Integer> path;
  private Integer depth;

  public String pathAsString() {
    return String.join(
        ".",
        path().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(".")));
  }

}