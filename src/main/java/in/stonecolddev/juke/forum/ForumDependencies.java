package in.stonecolddev.juke.forum;

import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Accessors(fluent = true)
@Component
public class ForumDependencies {

  private final DatabaseConfiguration databaseConfiguration;

  public ForumDependencies(
      DatabaseConfiguration databaseConfiguration
  ) {
    this.databaseConfiguration = databaseConfiguration;

  }

}