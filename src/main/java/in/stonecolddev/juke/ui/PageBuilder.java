package in.stonecolddev.juke.ui;

import java.util.Map;

public interface PageBuilder {

  Map<String, Object> compileForView(String pageSlug) throws Throwable;

}