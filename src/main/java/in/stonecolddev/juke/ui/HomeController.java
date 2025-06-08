package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

  private final Logger log = LoggerFactory.getLogger(HomeController.class);

  private final DefaultPageHandler pageBuilder;

  public HomeController(
      DefaultPageHandler pageBuilder) {
    this.pageBuilder = pageBuilder;
  }

  @GetMapping("/")
  public ModelAndView home() {

    ModelAndView mv = new ModelAndView("index");
    // TODO: it would be cool if we could have a definition for a given page and retrieving it by name
    //       pulls in all of those components from the db
    //       e.g. Page page = Page.builder().layout(layoutName).build();
    //            // ...
    //            page.withTitle("New Title");
    //            page.withModule(Module.Poll.builder().name("Poll Name").options(List.of(...)));
    //       or define it in the database with a dsl/template language somehow
    mv.addAllObjects(pageBuilder.compileForView("front-page"));

    mv.addObject("motd", pageBuilder.motd());
    mv.addObject("news", pageBuilder.news());

    return mv;
  }

}