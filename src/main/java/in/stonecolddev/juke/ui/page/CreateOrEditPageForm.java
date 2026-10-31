package in.stonecolddev.juke.ui.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrEditPageForm {

  // TODO: account for children/parents
  // TODO: need an approval field
  private String title;
  private String body;
  private Boolean approved;
  private Set<String> children;
  private Optional<String> parent;
  private LocalDateTime publishedOn;
  private String slug;

}