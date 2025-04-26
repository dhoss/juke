package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

  private final Logger log = LoggerFactory.getLogger(HomeController.class);

  private final DefaultPageBuilder pageBuilder;

  public HomeController(DefaultPageBuilder pageBuilder) {
    this.pageBuilder = pageBuilder;
  }

  @GetMapping("/")
  public ModelAndView home() {

    ModelAndView mv = new ModelAndView("index");
    mv.addObject("page", pageBuilder.findPage("front-page"));
    mv.addObject("newsItems", pageBuilder.news());

    return mv;
  }

}