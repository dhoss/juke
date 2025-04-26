package in.stonecolddev.juke.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class Page {
  private String title;
  private OffsetDateTime publishedOn;
  private Author author;
  private Map<String, PageComponent> components;
  private String slug;

  public enum ComponentType {
    MOTD,
    POST,
    NEWS,
    HEADER,
    FOOTER,
    TOP_NAV_BAR
  }
}