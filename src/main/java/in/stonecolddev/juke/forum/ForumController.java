package in.stonecolddev.juke.forum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ForumController {

  private final ForumDependencies forumDependencies;

  public ForumController(ForumDependencies forumDependencies) {
    this.forumDependencies = forumDependencies;
  }

  // TODO: paginate
  @GetMapping("/forums")
  public ModelAndView forums() {

    // TODO: build an api for this
    return new ModelAndView("forums/list");

  }

  @GetMapping("/forums/{forumSlug}")
  public ModelAndView viewForum(@PathVariable("forumSlug") String forumSlug) {
    return new ModelAndView("forums/view_forum");
  }

}