package in.stonecolddev.juke.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MetricsConfiguration {

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

}