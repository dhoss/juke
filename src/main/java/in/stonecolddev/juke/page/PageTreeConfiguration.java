package in.stonecolddev.juke.page;

import in.stonecolddev.juke.data.storage.tree.DatabaseTreeConfiguration;
import in.stonecolddev.juke.data.storage.tree.DatabaseTreeConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.Set;

@Configuration
@Profile({"local", "unit-test", "it-test", "dev", "prod"})
public class PageTreeConfiguration {

  @Bean
  public DatabaseTreeConfiguration configuration() {
    return DatabaseTreeConfigurationBuilder.builder()
        .idColumn("id")
        .treeTableAlias("p")
        .queryParameters(Map.of("author", "devin"))
        .anchorQueryColumnSet(
            Set.of(
                "author",
                "title",
                "body",
                "approved",
                "created_on",
                "published_on",
                "parent"))
        .treeTable("page_trees")
        .parentColumn("parent")
        .remainingCteQueryColumnsSet(Set.of("author_id", "email"))
        .whereColumn("slug")
        .build();
  }

}
