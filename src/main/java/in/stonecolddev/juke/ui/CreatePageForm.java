package in.stonecolddev.juke.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class CreatePageForm {

  private String title;
  private String body;
  private LocalDateTime publishedOn;

  public LocalDateTime getPublishedOn() {
    return this.publishedOn;
  }

}