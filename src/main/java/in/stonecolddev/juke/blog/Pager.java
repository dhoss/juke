package in.stonecolddev.juke.blog;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Optional;

@Builder
@Accessors(fluent = true)
@Data
public class Pager {

  Optional<BlogPost> forThread;
  Integer lastSeen;
  Integer pageSize;

  // TODO: get page from last seen + page size
}