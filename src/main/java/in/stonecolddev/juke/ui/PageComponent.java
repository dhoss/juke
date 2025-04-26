package in.stonecolddev.juke.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class PageComponent {
  private String title;
  private OffsetDateTime publishedOn;
  private Author author;
  private Page.ComponentType type;
  private String body;
}