package in.stonecolddev.juke.ui;


import in.stonecolddev.juke.configuration.JukeConfiguration;
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

  private final JukeConfiguration jukeConfiguration;

  private final DefaultPageBuilder pageBuilder;

  public HomeController(
      JukeConfiguration jukeConfiguration,
      DefaultPageBuilder pageBuilder) {
    this.jukeConfiguration = jukeConfiguration;
    this.pageBuilder = pageBuilder;
  }

  @GetMapping("/")
  public ModelAndView home() {

    ModelAndView mv = new ModelAndView("index");
    mv.addObject("page", pageBuilder.findPage("front-page"));
    mv.addObject("newsItems", pageBuilder.news());
    log.debug("******* CONFIG {}", jukeConfiguration);
    // TODO: figure out why this isn't working
    mv.addObject("jukeAppVersion", jukeConfiguration.version());

    return mv;
  }

}