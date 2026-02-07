package in.stonecolddev.juke.forum.schema;

import in.stonecolddev.juke.user.JukeUser;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;


@Builder
@Accessors(fluent = true)
@Data
public class ForumEntity implements ForumDatabaseRecord {

  private final Integer id;

  private final String title;

  private final String slug;

  private final String description;

  private final OffsetDateTime createdOn;

  private final JukeUser createdBy;

  private final JukeUser updatedBy;

  private final OffsetDateTime updatedOn;

  private final List<ForumThreadEntity> threads;

  private final List<JukeUser> moderators;

  private final Boolean isDeleted;

}