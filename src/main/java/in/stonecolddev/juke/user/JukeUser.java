package in.stonecolddev.juke.user;


import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.experimental.Accessors;

import java.util.List;

@Builder
@Accessors(fluent = true)
@With
@Data
public class JukeUser {

  private final Integer id;
  private final String userName;
  private final String password;
  private final String email;
  private final List<String> roles;

}