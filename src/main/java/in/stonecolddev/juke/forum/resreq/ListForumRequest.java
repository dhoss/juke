package in.stonecolddev.juke.forum.resreq;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Builder
@Accessors(fluent = true)
@Data
public class ListForumRequest {

  private final Integer page;
  private final Integer pageSize;
  // TODO: make this an object or predicate
  private final Map<String, String> sorting;

}