package in.stonecolddev.juke.metrics;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class PerRequestMetricsCollector implements WithMetrics {

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

  public MeterRegistry meterRegistry() {
    return meterRegistry;
  }

  public void incrementPageQueryCounter() {
    findOrCreateCounter("pageQueryCounter").increment();
  }

}