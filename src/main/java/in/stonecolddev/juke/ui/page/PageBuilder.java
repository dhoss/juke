package in.stonecolddev.juke.ui.page;

import java.util.Map;

public interface PageBuilder {

  Map<String, Object> compileForView(String pageSlug) throws Throwable;

}