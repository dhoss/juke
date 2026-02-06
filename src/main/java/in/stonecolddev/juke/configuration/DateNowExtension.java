package in.stonecolddev.juke.configuration;

import io.pebbletemplates.pebble.extension.AbstractExtension;

import java.time.OffsetDateTime;
import java.util.Map;

public class DateNowExtension extends AbstractExtension {
  @Override
  public Map<String, Object> getGlobalVariables() {
    return Map.of("now", OffsetDateTime.now());
  }
}