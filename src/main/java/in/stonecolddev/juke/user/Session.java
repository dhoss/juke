package in.stonecolddev.juke.user;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.experimental.Accessors;

import java.time.Instant;

@Builder
@With
@Accessors(fluent = true)
@Data
public class Session {

  private String primaryId;
  private String sessionId;
  private Instant creationTime;
  private Instant lastAccessTime;
  private Integer maxInactiveInterval;
  private Instant expiryTime;
  private JukeUser user;

}