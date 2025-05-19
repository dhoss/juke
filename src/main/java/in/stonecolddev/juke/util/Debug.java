package in.stonecolddev.juke.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Debug {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String prettyObject(Object object) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}