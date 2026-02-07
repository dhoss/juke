package in.stonecolddev.juke.forum.configuration;

import in.stonecolddev.juke.forum.storage.ForumStorage;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Accessors(fluent = true)
@Component
public class ForumDependencies {

  private final ForumConfiguration databaseConfiguration;

  private final ForumStorage forumStorage;

  public ForumDependencies(
      ForumConfiguration forumConfiguration,
      ForumStorage forumStorage
  ) {
    this.databaseConfiguration = forumConfiguration;
    this.forumStorage = forumStorage;

  }

}