package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class AdminController {


  private final Logger log = LoggerFactory.getLogger(AdminController.class);

  private final DefaultPageHandler pageHandler;

  public AdminController(
      DefaultPageHandler pageHandler
  ) {
    this.pageHandler = pageHandler;
  }

  @GetMapping
  public ModelAndView home() {
    ModelAndView mv = new ModelAndView("admin/home.html");

    return mv;
  }

  @GetMapping("/pages/new")
  public ModelAndView newPage() {
    ModelAndView mv = new ModelAndView("admin/pages/new");

    mv.addObject("page", new CreatePageForm());

    return mv;
  }

  @PostMapping("/pages/create")
  public String createPage(@ModelAttribute CreatePageForm pageFormData) {

    // TODO: validation/error handling
    pageHandler.createPage(pageFormData);

    return "redirect:/admin/pages";
  }

  @GetMapping("/pages")
  public ModelAndView listPages() {
    ModelAndView mv = new ModelAndView("admin/pages/list");

    mv.addObject("pages", pageHandler.listPages());

    return mv;
  }

}