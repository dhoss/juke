package in.stonecolddev.juke.ui;


import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.TimeUnit;

@Controller
public class HomeController {

  private final Logger log = LoggerFactory.getLogger(HomeController.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final DefaultPageBuilder pageBuilder;

  public HomeController(
      PerRequestMetricsCollector perRequestMetricsCollector,
      DefaultPageBuilder pageBuilder) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.pageBuilder = pageBuilder;
  }

  @GetMapping("/")
  public ModelAndView home() {

    MeterRegistry registry = perRequestMetricsCollector.meterRegistry();
    Timer.Sample pageConstructionSample = Timer.start(registry);
    ModelAndView mv = new ModelAndView("index");
    mv.addObject("page", pageBuilder.findPage("front-page"));
    mv.addObject("newsItems", pageBuilder.news());
    mv.addObject("pageQueryCounter", pageBuilder.pageMetrics().counterValue("pageQueryCounter"));
    pageConstructionSample.stop(registry.timer("pageConstructionTimer"));
    mv.addObject("pageConstructionTimer", registry.timer("pageConstructionTimer").totalTime(TimeUnit.SECONDS));

    return mv;
  }

}