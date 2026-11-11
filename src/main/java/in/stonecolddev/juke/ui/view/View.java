package in.stonecolddev.juke.ui.view;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Data
@Builder
@Accessors(fluent = true)
public class View {

  // TODO: this could handle taking a result Optional from the db/cache
  //       and dealing with the orElseThrow so that doesn't have to be done
  //       in every endpoint
  private String name;
  private Map<String, Object> templateData;

  public static class ViewBuilder {

    public ModelAndView toView() {
      return new ModelAndView(name).addAllObjects(templateData);
    }

    // TODO: figure out RedirectAttributes stuff
    public ModelAndView redirect() {
      return new ModelAndView("redirect:" + name);
    }
  }

}