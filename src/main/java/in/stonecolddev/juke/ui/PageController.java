package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {

  private final Logger log = LoggerFactory.getLogger(PageController.class);

  private final DefaultPageBuilder pageBuilder;

  public PageController(
      DefaultPageBuilder pageBuilder) {
    this.pageBuilder = pageBuilder;
  }

  @GetMapping("/{pageSlug}.html")
  public ModelAndView findPage(@PathVariable("pageSlug") String pageSlug) {
    ModelAndView mv = new ModelAndView("pages/page.html");

    return mv;
  }
}