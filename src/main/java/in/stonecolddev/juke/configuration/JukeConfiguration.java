package in.stonecolddev.juke.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ConfigurationProperties(prefix = "juke")
@Profile({"local", "unit-test", "it-test", "dev", "prod"})
@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class JukeConfiguration {
  private String version;
}