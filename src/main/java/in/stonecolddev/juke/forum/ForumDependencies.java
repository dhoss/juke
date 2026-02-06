package in.stonecolddev.juke.forum;

import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Accessors(fluent = true)
@Component
public class ForumDependencies {

  private final ForumDatabaseConfiguration databaseConfiguration;

  public ForumDependencies(
      ForumDatabaseConfiguration databaseConfiguration
  ) {
    this.databaseConfiguration = databaseConfiguration;

  }

}