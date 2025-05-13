package in.stonecolddev.juke.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageComponentEntityResultSetExtractor implements ResultSetExtractor<List<PageComponentEntity>> {

  private final Logger log = LoggerFactory.getLogger(PageComponentEntityResultSetExtractor.class);


  @Override
  public List<PageComponentEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {

    PageComponentEntity.PageComponentEntityBuilder pageComponentEntityBuilder = PageComponentEntity.builder();
    List<PageComponentEntity> pageComponentEntities = new ArrayList<>();
    log.info("Building page component entity");
    while (rs.next()) {
      pageComponentEntityBuilder.id(rs.getInt("id"));
      pageComponentEntityBuilder.author(
          AuthorEntity.builder()
              .id(rs.getInt("page_component_author_id"))
              .userName(rs.getString("page_component_author"))
              .build());
      pageComponentEntityBuilder.title(rs.getString("page_component_title"));
      pageComponentEntityBuilder.body(rs.getString("page_component_body"));
      pageComponentEntityBuilder.slug(rs.getString("page_component_slug"));
      pageComponentEntityBuilder.type(PageComponent.ComponentType.valueOf(rs.getString("page_component_type").toUpperCase()));
      pageComponentEntityBuilder.publishedOn(rs.getObject("page_component_published_on", OffsetDateTime.class));

      pageComponentEntities.add(pageComponentEntityBuilder.build());

    }

    return pageComponentEntities;
  }
}