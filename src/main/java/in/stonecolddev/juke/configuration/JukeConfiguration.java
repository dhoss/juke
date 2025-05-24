package in.stonecolddev.juke.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "juke")
@Profile({"local", "unit-test", "it-test", "dev", "prod"})
@Data
public class JukeConfiguration {
  // TODO: combine this with database configuration
  private String version;
}