package in.stonecolddev.juke.ui;


import in.stonecolddev.juke.configuration.JukeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

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
    mv.addAllObjects(pageBuilder.compileForView("front-page"));

    mv.addObject("jukeAppVersion", jukeConfiguration.getVersion());

    return mv;
  }

}