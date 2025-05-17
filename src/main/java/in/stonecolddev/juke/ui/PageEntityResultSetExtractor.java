package in.stonecolddev.juke.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PageEntityResultSetExtractor implements ResultSetExtractor<PageEntity> {

  private final Logger log = LoggerFactory.getLogger(PageEntityResultSetExtractor.class);


  @Override
  public PageEntity extractData(ResultSet rs) throws SQLException, DataAccessException {

    PageEntity.PageEntityBuilder pageEntityBuilder = PageEntity.builder();
    //List<PageComponentEntity> pageComponents = new ArrayList<>();
    //PageComponentEntity.PageComponentEntityBuilder pageComponentBuilder = PageComponentEntity.builder();

    List<SidebarMenuEntity> sidebarMenuEntities = new ArrayList<>();
    Map<Integer, SidebarMenuEntity> sidebarMenuEntityMap = new HashMap<>();
    List<SidebarMenuItemEntity> sidebarMenuItemEntities = new ArrayList<>();
    Map<Integer, SidebarMenuItemEntity> sidebarMenuItemEntityMap = new HashMap<>();

    log.info("Building page entity");
    while (rs.next()) {


      pageEntityBuilder.id(rs.getInt("page_id"));
      pageEntityBuilder.author(
          AuthorEntity.builder()
              .id(rs.getInt("author_id"))
              .userName(rs.getString("page_author"))
              .build());

      Integer parentSidebarId = rs.getInt("sidebar_id");
      log.debug("**** SIDEBAR ID {}", parentSidebarId);

      SidebarMenuEntity.SidebarMenuEntityBuilder sidebarMenuEntityBuilder = SidebarMenuEntity.builder();
      sidebarMenuEntityBuilder.id(parentSidebarId)
          .title(rs.getString("sidebar_title"));
      sidebarMenuEntityMap.put(
          parentSidebarId, sidebarMenuEntityBuilder.sidebarMenuItems(sidebarMenuItemEntities).build());

      sidebarMenuItemEntityMap.put(
          parentSidebarId,
          SidebarMenuItemEntity.builder()
              .id(rs.getInt("sidebar_item_id"))
              .sidebarMenuId(parentSidebarId)
              .title(rs.getString("sidebar_item_title"))
              .body(rs.getString("sidebar_item_body"))
              .build());


      for (Integer key : sidebarMenuItemEntityMap.keySet()) {
        if (Objects.equals(key, parentSidebarId)) {
          sidebarMenuItemEntities.add(sidebarMenuItemEntityMap.get(key));
        }
      }



//      pageComponentBuilder.id(rs.getInt("page_component_id"));
//      pageComponentBuilder.title(rs.getString("page_component_title"));
//
//      // This shouldn't happen
//      if (rs.getInt("page_component_author_id") != 0) {
//        pageComponentBuilder.author(AuthorEntity.builder()
//            .id(rs.getInt("page_component_author_id"))
//            .userName(rs.getString("page_component_author_name"))
//            .build());
//      }
//
//      pageComponentBuilder.body(rs.getString("page_component_body"));
//      String pageComponentType = rs.getString("page_component_type");
//      pageComponentBuilder.type(PageComponent.ComponentType.valueOf(pageComponentType.toUpperCase()));
//      pageComponentBuilder.publishedOn(rs.getObject("page_component_published_on", OffsetDateTime.class));
//      pageComponents.add(pageComponentBuilder.build());
//

      pageEntityBuilder.title(rs.getString("page_title"));
      pageEntityBuilder.publishedOn(rs.getObject("page_published_on", OffsetDateTime.class));

    }

   // log.debug("**** PAGE COMPONENTS IN RS {}", pageComponents);

   // pageEntityBuilder.pageComponents(pageComponents);
    //   forEach((k, v) -> sidebarMenuEntities.add(v));
    //sidebarMenuEntityMap.forEach((k, v) -> sidebarMenuEntities.add(v));
    pageEntityBuilder.sidebarMenus(new ArrayList<>(sidebarMenuEntityMap.values()));//sidebarMenuEntities);

    return pageEntityBuilder.build();
  }
}