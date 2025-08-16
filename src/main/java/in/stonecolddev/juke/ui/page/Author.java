package in.stonecolddev.juke.ui.page;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor // for modelmapper
@AllArgsConstructor
@Accessors(fluent = true)
@With
public class Author {

  private String userName;
  private String email;
  private Integer id;

  public static Author create(String userName) {
    return Author.builder().userName(userName).build();
  }

}