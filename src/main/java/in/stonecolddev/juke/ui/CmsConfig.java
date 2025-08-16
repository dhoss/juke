package in.stonecolddev.juke.ui;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Builder
@Accessors(fluent = true)
@Data
public class CmsConfig {

  String layoutSlug;
  Integer layoutId;
  String timezone;

}