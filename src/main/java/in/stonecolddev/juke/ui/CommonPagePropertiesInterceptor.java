package in.stonecolddev.juke.ui;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class CommonPagePropertiesInterceptor implements HandlerInterceptor {

  private final Logger log = LoggerFactory.getLogger(CommonPagePropertiesInterceptor.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;


  private Timer.Sample pageConstructionSample;

  private MeterRegistry registry;

  public CommonPagePropertiesInterceptor(PerRequestMetricsCollector perRequestMetricsCollector) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    registry = perRequestMetricsCollector.meterRegistry();
    pageConstructionSample = Timer.start(registry);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) {
    log.info("Populating common page data");
    pageConstructionSample.stop(registry.timer("pageConstructionTimer"));
    assert mv != null;
    mv.addAllObjects(
        Map.of(
            "pageConstructionTimer", registry.timer("pageConstructionTimer").totalTime(TimeUnit.SECONDS),
            "pageQueryCounter", perRequestMetricsCollector.counterValue("pageQueryCounter")));
  }

}