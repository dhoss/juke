package in.stonecolddev.juke.ui;


import in.stonecolddev.juke.configuration.JukeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

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
    Map<String, Object> modelMap = pageBuilder.compileForView("front-page");
    log.debug("***** COMPILE FOR VIEW {}", modelMap);
   // mv.addAllObjects(modelMap);
    mv.addObject("page", pageBuilder.findPage("front-page"));
    mv.addObject("motdItems", List.of(Map.of("title", "motd title", "publishedOn", "date", "body", "motd body")));
    mv.addObject("newsItems", pageBuilder.news());
    mv.addObject("sidebarItems",
        List.of(
            Map.of(
                "title", "sidebar title", "publishedOn", "date", "body", "sidebar body"),
            Map.of(
                "title", "sidebar title2", "publishedOn", "date", "body", "sidebar body2")
        ));// pageBuilder.news());
    // TODO: figure out why this isn't working
    mv.addObject("jukeAppVersion", jukeConfiguration.version());

    return mv;
  }

}