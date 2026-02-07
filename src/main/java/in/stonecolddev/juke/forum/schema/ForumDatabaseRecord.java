package in.stonecolddev.juke.forum.schema;

import in.stonecolddev.juke.user.JukeUser;

import java.time.OffsetDateTime;

public interface ForumDatabaseRecord {

  Integer id();

  OffsetDateTime updatedOn();

  OffsetDateTime createdOn();

  JukeUser createdBy();

  default JukeUser owner() {
    return createdBy();
  }

  JukeUser updatedBy();

  Boolean isDeleted();

}