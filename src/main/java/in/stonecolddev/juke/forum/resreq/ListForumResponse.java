package in.stonecolddev.juke.forum.resreq;


import in.stonecolddev.juke.forum.schema.ForumEntity;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Builder
@Accessors(fluent = true)
@Data
public class ListForumResponse {

  // TODO: return a representation that's not an entity
  List<ForumEntity> forums;

}