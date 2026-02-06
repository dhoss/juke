package in.stonecolddev.juke.forum;

import in.stonecolddev.juke.user.JukeUser;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Builder
@Accessors(fluent = true)
@Data
public class ForumThreadEntity implements ForumDatabaseRecord {

  @NonNull
  private final Integer id;

  @NonNull
  private final JukeUser createdBy;

  private final JukeUser updatedBy;

  @NonNull
  private final OffsetDateTime createdOn;

  private final OffsetDateTime updatedOn;

  private final Boolean isDeleted;

  @NonNull
  private final String title;

  @NonNull
  private final String slug;

  private final ForumThreadEntity parent;

  private final List<ForumThreadEntity> replies;

  public Boolean isThreadRoot() {
    return Optional.ofNullable(parent).isPresent();
  }

}