package in.stonecolddev.juke.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

public class PageEntityResultSetExtractor implements ResultSetExtractor<PageEntity> {

  private final Logger log = LoggerFactory.getLogger(PageEntityResultSetExtractor.class);

  @Override
  public PageEntity extractData(ResultSet rs) throws SQLException, DataAccessException {

    PageEntity.PageEntityBuilder pageEntityBuilder = PageEntity.builder();

    Set<SidebarMenuEntity> sidebarMenuEntities = new HashSet<>();
    Map<String, SidebarMenuItemEntity> sidebarMenuItemEntityMap = new HashMap<>();

    log.info("Building page entity");
    while (rs.next()) {

      pageEntityBuilder.id(rs.getInt("page_id"));
      pageEntityBuilder.author(
          AuthorEntity.builder()
              .id(rs.getInt("author_id"))
              .userName(rs.getString("page_author"))
              .build());

      SidebarMenuEntity.SidebarMenuEntityBuilder sidebarMenuEntityBuilder = SidebarMenuEntity.builder();
      sidebarMenuEntityBuilder.id(rs.getInt("sidebar_id"))
          .title(rs.getString("sidebar_title"))
          .sidebarMenuItems(new ArrayList<>());

      SidebarMenuItemEntity sidebarMenuItemEntity = SidebarMenuItemEntity.builder()
          .id(rs.getInt("sidebar_item_id"))
          .sidebarMenuId(rs.getInt("parent_sidebar_id"))
          .title(rs.getString("sidebar_item_title"))
          .body(rs.getString("sidebar_item_body"))
          .build();

      // add the sidebar item to the map with a semi unique id so it's easier to retrieve
      sidebarMenuItemEntityMap.put(
          sidebarMenuItemEntity.id() + "" + sidebarMenuItemEntity.sidebarMenuId(), sidebarMenuItemEntity);

      sidebarMenuEntities.add(sidebarMenuEntityBuilder.build());

      pageEntityBuilder.title(rs.getString("page_title"));
      pageEntityBuilder.publishedOn(rs.getObject("page_published_on", OffsetDateTime.class));

      pageEntityBuilder.body(rs.getString("page_body"));

    }

    List<SidebarMenuEntity> mergedSidebarMenuEntities = new ArrayList<>();
    for (SidebarMenuEntity sidebarMenuEntity : sidebarMenuEntities) {
      var currentSidebarItems = sidebarMenuEntity.sidebarMenuItems();
      if (currentSidebarItems == null) {
        currentSidebarItems = new ArrayList<>();
      }
      for (var entry : sidebarMenuItemEntityMap.entrySet()) {
        if (Objects.equals(entry.getKey(), entry.getValue().id() + "" + sidebarMenuEntity.id())) {
          currentSidebarItems.add(entry.getValue());
          sidebarMenuEntity = sidebarMenuEntity.toBuilder().sidebarMenuItems(currentSidebarItems).build();
        }
      }
      mergedSidebarMenuEntities.add(sidebarMenuEntity);
    }

    pageEntityBuilder.sidebarMenus(mergedSidebarMenuEntities);

    return pageEntityBuilder.build();
  }
}