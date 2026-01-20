package in.stonecolddev.juke.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class Time {

  // TODO: refactor to pull from config
  @Bean
  public Clock systemClock() {
    return Clock.systemDefaultZone();
  }
}