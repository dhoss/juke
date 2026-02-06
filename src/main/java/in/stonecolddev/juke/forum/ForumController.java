package in.stonecolddev.juke.forum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ForumController {

  // TODO: paginate
  @GetMapping("/forums")
  public ModelAndView forums() {

    // TODO: build an api for this
    return new ModelAndView("forums/list");

  }

}