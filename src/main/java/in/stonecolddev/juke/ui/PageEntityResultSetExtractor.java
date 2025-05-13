package in.stonecolddev.juke.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class PageEntityResultSetExtractor implements ResultSetExtractor<PageEntity> {

  private final Logger log = LoggerFactory.getLogger(PageEntityResultSetExtractor.class);


  @Override
  public PageEntity extractData(ResultSet rs) throws SQLException, DataAccessException {

    PageEntity.PageEntityBuilder pageEntityBuilder = PageEntity.builder();
    Map<String, PageComponentEntity> pageComponents = new HashMap<>();
    PageComponentEntity.PageComponentEntityBuilder pageComponentBuilder = PageComponentEntity.builder();

    log.info("Building page entity");
    while (rs.next()) {

      pageEntityBuilder.id(rs.getInt("id"));
      pageEntityBuilder.author(
          AuthorEntity.builder()
              .id(rs.getInt("author_id"))
              .userName(rs.getString("page_author"))
              .build());

      pageComponentBuilder.id(rs.getInt("page_component_id"));
      pageComponentBuilder.title(rs.getString("page_component_title"));

      // This shouldn't happen
      if (rs.getInt("page_component_author_id") != 0) {
        pageComponentBuilder.author(AuthorEntity.builder()
            .id(rs.getInt("page_component_author_id"))
            .userName(rs.getString("page_component_author_name"))
            .build());
      }

      pageComponentBuilder.body(rs.getString("page_component_body"));
      String pageComponentType = rs.getString("page_component_type");
      pageComponentBuilder.type(PageComponent.ComponentType.valueOf(pageComponentType.toUpperCase()));
      pageComponentBuilder.publishedOn(rs.getObject("page_component_published_on", OffsetDateTime.class));
      pageComponents.put(pageComponentType, pageComponentBuilder.build());

      pageEntityBuilder.title(rs.getString("page_title"));
      pageEntityBuilder.publishedOn(rs.getObject("page_published_on", OffsetDateTime.class));

    }

    pageEntityBuilder.pageComponents(pageComponents);

    return pageEntityBuilder.build();
  }
}