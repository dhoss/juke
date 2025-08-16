package in.stonecolddev.juke.metrics;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class Stats {

  private final Integer usersOnline;
  private final Integer members;

}