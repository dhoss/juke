package in.stonecolddev.juke.ui;

import in.stonecolddev.juke.ui.page.PageNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class HttpExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(HttpExceptionHandler.class);


  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PageNotFoundException.class)
  public ModelAndView pageNotFoundExceptionHandler(HttpServletRequest req, PageNotFoundException e) {

    log.debug("***** THROWING NOT FOUND ERROR");
    ModelAndView mv = new ModelAndView();
    mv.addObject("exception", e);
    mv.addObject("url", req.getRequestURL());
    mv.setViewName("error");
    return mv;

  }
}