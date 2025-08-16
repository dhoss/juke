package in.stonecolddev.juke.ui.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrEditPageForm {

  private String title;
  private String body;
  private LocalDateTime publishedOn;
  private String slug;

}