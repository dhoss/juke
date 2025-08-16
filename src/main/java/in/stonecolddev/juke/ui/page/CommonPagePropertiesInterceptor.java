package in.stonecolddev.juke.ui.page;

import in.stonecolddev.juke.configuration.JukeConfiguration;
import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import in.stonecolddev.juke.ui.ConfigGlob;
import in.stonecolddev.juke.user.SessionService;
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

  private final JukeConfiguration jukeConfiguration;

  private final SessionService sessionService;

  private Timer.Sample pageConstructionSample;

  private MeterRegistry registry;

  private final ConfigGlob configGlob;

  public CommonPagePropertiesInterceptor(
      JukeConfiguration jukeConfiguration,
      PerRequestMetricsCollector perRequestMetricsCollector,
      ConfigGlob configGlob,
      SessionService sessionService) {
    this.sessionService = sessionService;
    this.jukeConfiguration = jukeConfiguration;
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.configGlob = configGlob;
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
    String v1 = configGlob.configuration().layoutSlug();
    log.debug("***** LAYOUT SLUG {}", v1);
    mv.addAllObjects(
        Map.of(
            "layoutSlug", v1,
            "pageConstructionTimer", registry.timer("pageConstructionTimer").totalTime(TimeUnit.SECONDS),
            // TODO: figure out how to get pageQueryCounter updated with the query counts from below
            "pageQueryCounter", perRequestMetricsCollector.counterValue("pageQueryCounter"),
            "jukeAppVersion", jukeConfiguration.getVersion(),
            "userSession", sessionService.findSession(request.getUserPrincipal()),
                "stats", sessionService.stats() // TODO: THIS SHOULD BE IN ITS OWN CLASS
            ));
  }

}