package in.stonecolddev.juke.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    // TODO: figure out why exceptions aren't being handled
    mv.addAllObjects(pageHandler.compileForView(pageSlug));

    return mv;
  }


  @GetMapping("/admin/pages/new")
  public ModelAndView newPage() {
    ModelAndView mv = new ModelAndView("admin/pages/new");

    mv.addObject("page", new CreatePageForm());

    return mv;
  }

  @GetMapping("/admin/pages/{slug}/edit")
  public ModelAndView editPage(@PathVariable("slug") String pageSlug) {
    ModelAndView mv = new ModelAndView("admin/pages/edit");
    Page page = pageHandler.findPage(pageSlug)
        .orElseThrow(() -> new PageNotFoundException("Page not found with slug " + pageSlug));

    CreatePageForm build = CreatePageForm.builder()
        .title(page.title())
        .body(page.body())
        .publishedOn(page.publishedOn().toLocalDateTime())
        .build();
    log.debug("**** PUBLISHED ON {}", build.publishedOn());
    mv.addObject("page",
        build
    );

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