package in.stonecolddev.juke.ui;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultPageBuilder implements PageBuilder {

  private final Logger log = LoggerFactory.getLogger(DefaultPageBuilder.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final ModelMapper mapper;

  // TODO: make this a fluent api, fuck it why not
  public DefaultPageBuilder(
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      ModelMapper mapper
  ) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    this.mapper = mapper;
  }

  public Page findPage(String slug) {
    Map<String, String> configuration = configuration();
    log.debug("**** CONFIGURATION {}", configuration);

    SqlParameterSource namedParameters =
        new MapSqlParameterSource()
            .addValue("slug", slug)
            .addValue("layoutId", Integer.parseInt(configuration.get("layoutId")));

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();



    return mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
            """
            select
              sb.title as "sidebar_title"
            , sb.id as "sidebar_id"
            , sbi.id as "sidebar_item_id"
            , sbi.title as "sidebar_item_title"
            , sbi.body as "sidebar_item_body"
            , p.id as "page_id"
            , p.published_on as "page_published_on"
            , p.title as "page_title"
            , a.user_name as "page_author"
            , a.id as "author_id"
            from pages p
            left join sidebar_menus sb on sb.layout_id = :layoutId
            left join sidebar_menu_items sbi on sbi.sidebar_menus_id = sb.id
            left join authors a on a.id = p.author_id
            where p.slug = :slug
            and p.is_deleted = false
            """,
           // """
           //     select
           //         pc.title as "page_component_title"
           //       , pc.id as "page_component_id"
           //       , pc.type as "page_component_type"
           //       , pc.body as "page_component_body"
           //       , pc.published_on as "page_component_published_on"
           //       , pc.author_id as "page_component_author_id"
           //       , pca.user_name as "page_component_author_name"
           //       , p.id as "page_id"
           //       , p.published_on as "page_published_on"
           //       , p.title as "page_title"
           //       , a.user_name as "page_author"
           //       , a.id as "author_id"
           //     from pages p
           //     inner join page_components_to_page_mappings pcm on p.id = pcm.page_id
           //     left join page_components pc on pc.id = pcm.page_component_id
           //     left join authors a on a.id = p.author_id
           //     left join authors pca on pca.id = pc.author_id
           //     where p.slug = 'front-page'
           //     and p.is_deleted = false
           //     order by pc.published_on desc
           //     """,
        namedParameters,
        new PageEntityResultSetExtractor()), Page.class);
  }

  // TODO: this is only being used in findPage, so figure out how to inline it there
//  public Page basePage() {
//
//    // TODO: make an enum of counters we're tracking and use them instead of strings
//    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();
//
//    return mapper.map(
//        namedParameterJdbcTemplate.query(
//            // TODO: pull common query pieces out and compose queries from them
//            """
//                    select
//                        pc.title as "page_component_title"
//                      , pc.id as "page_component_id"
//                      , pc.type as "page_component_type"
//                      , pc.body as "page_component_body"
//                      , pc.published_on as "page_component_published_on"
//                      , pc.author_id as "page_component_author_id"
//                      , pca.user_name as "page_component_author_name"
//                      , p.id as "page_id"
//                      , p.published_on as "page_published_on"
//                      , p.title as "page_title"
//                      , a.user_name as "page_author"
//                      , a.id as "author_id"
//                    from pages p
//                    inner join page_components_to_page_mappings pcm on p.id = pcm.page_id
//                    left join page_components pc on pc.id = pcm.page_component_id
//                    left join authors a on a.id = p.author_id
//                    left join authors pca on pca.id = pc.author_id
//                    where p.slug = 'base-page'
//                    and p.is_deleted = false
//                    order by pc.published_on desc
//                """,
//            new PageEntityResultSetExtractor()), Page.class);
//  }

  public Map<String, String> configuration() {
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();

    return namedParameterJdbcTemplate.query(
        """
        select * from configuration
        """,
        rs -> {
          Map<String, String> kv = new HashMap<>();
          while (rs.next()) {
            kv.put("layoutId", rs.getString("layout_id"));
          }
          return kv;
        }
    );
  }

  public Map<String, Object> compileForView(String slug) {
    Map<String, Object> pageView = new HashMap<>();

    Page page = findPage(slug);
    pageView.put("sidebarItems", page.sidebarMenus());
  //  for (PageComponent.ComponentType type : PageComponent.ComponentType.values()) {
  //    List<PageComponent> c = new ArrayList<>();
  //    for (var component : page.components()) {
  //      if (component.type() == type) {
  //        c.add(component);
  //      }
  //    }
  //    pageView.put(type.name().toLowerCase() + "Items", c);
  //  }

    pageView.put("page", page);
    log.debug("**** PAGE VIEW {}", pageView);

    return pageView;
  }


}