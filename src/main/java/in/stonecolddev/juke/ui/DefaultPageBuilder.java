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
    this.mapper = mapper;
  }

  public Page findPage(String slug) {
    Map<String, String> configuration = configuration();

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
            , sbi.sidebar_menus_id as "parent_sidebar_id"
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
        namedParameters,
        new PageEntityResultSetExtractor()), Page.class);
  }

  // TODO: this may be better suited as a bean created on startup
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

  // TODO: really not sure we even need this
  public Map<String, Object> compileForView(String slug) {
    Map<String, Object> pageView = new HashMap<>();

    pageView.put("page", findPage(slug));

    return pageView;
  }
}