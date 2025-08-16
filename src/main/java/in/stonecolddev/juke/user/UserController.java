package in.stonecolddev.juke.user;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class UserController {

  private final Logger log = LoggerFactory.getLogger(UserController.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final DatabaseUserService databaseUserService;

  public UserController(
      PerRequestMetricsCollector perRequestMetricsCollector,
      DatabaseUserService databaseUserService
  ) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.databaseUserService = databaseUserService;
  }

  @GetMapping("/users/new")
  public ModelAndView newUser() {
    ModelAndView mv = new ModelAndView("/users/new");

    mv.addAllObjects(
        Map.of(
            "user", new CreateOrEditUserForm(),
            "action", "/users/create"
        )
    );

    return mv;
  }

  @GetMapping("/users/welcome")
  public ModelAndView welcome() {
    return new ModelAndView("/users/create-user-result.html");
  }

  @PostMapping("/users/create")
  public ModelAndView createUser(
      @ModelAttribute CreateOrEditUserForm userFormData,
      RedirectAttributes redirectAttributes) {

    if (userFormData.getPassword().equals(userFormData.getPasswordConfirm())) {
      databaseUserService.createUser(userFormData);
      redirectAttributes.addFlashAttribute("formSubmissionMessage", "User created!");
      redirectAttributes.addFlashAttribute("user", userFormData);
      return new ModelAndView("redirect:/users/welcome");
    }
    redirectAttributes.addFlashAttribute("formSubmissionMessage", "Passwords don't match");
    return new ModelAndView("redirect:/users/new");

  }

  @GetMapping("/login")
  public ModelAndView login() {
    return new ModelAndView("users/login");
  }

}