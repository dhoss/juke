package in.stonecolddev.juke.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public interface WithMetrics {

  MeterRegistry meterRegistry();

  default Counter findOrCreateCounter(String counterName) {
    return meterRegistry().counter(counterName);
  }

  default int counterValue(String counterName) {
    return (int)meterRegistry().counter(counterName).count();
  }
}