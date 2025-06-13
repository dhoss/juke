package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {

  private final Logger log = LoggerFactory.getLogger(PageController.class);

  private final DefaultPageHandler pageHandler;

  public PageController(
      DefaultPageHandler pageHandler) {
    this.pageHandler = pageHandler;
  }

  @GetMapping("/{pageSlug}.html")
  public ModelAndView findPage(@PathVariable("pageSlug") String pageSlug) {
    ModelAndView mv = new ModelAndView("pages/page");

    mv.addAllObjects(pageHandler.compileForView(pageSlug));

    return mv;
  }


  @GetMapping("/admin/pages/new")
  public ModelAndView newPage() {
    ModelAndView mv = new ModelAndView("admin/pages/new");

    mv.addObject("page", new CreatePageForm());

    return mv;
  }

  @PostMapping("/admin/pages/create")
  public String createPage(@ModelAttribute CreatePageForm pageFormData) {

    // TODO: validation/error handling
    pageHandler.createPage(pageFormData);

    return "redirect:/admin/pages";
  }

  @GetMapping("/admin/pages")
  public ModelAndView listPages() {
    ModelAndView mv = new ModelAndView("admin/pages/list");

    mv.addObject("pages", pageHandler.listPages());

    return mv;
  }
}