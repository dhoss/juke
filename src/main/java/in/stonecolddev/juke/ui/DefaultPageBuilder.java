package in.stonecolddev.juke.ui;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
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

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.incrementPageQueryCounter();

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
            new MapSqlParameterSource()
                .addValue("slug", slug)
                .addValue("layoutId", Integer.parseInt(configuration.get("layoutId"))),
        new PageEntityResultSetExtractor()), Page.class);
  }

  // TODO: this may be better suited as a bean created on startup
  public Map<String, String> configuration() {
    perRequestMetricsCollector.incrementPageQueryCounter();

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

  public News motd() {
    return findNews("motd").getFirst();
  }

  public List<News> news() {
    return findNews("news");
  }

  private List<News> findNews(String type) {
    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.incrementPageQueryCounter();

    // for mapping to a List
    mapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

    List<NewsEntity> newsEntities = namedParameterJdbcTemplate.query(
        """
            select
              n.id as "news_id"
            , n.title as "news_title"
            , n.body as "news_body"
            , n.type as "news_type"
            , n.created_on as "news_created_on"
            , n.published_on as "news_published_on"
            , a.id as "author_id"
            , a.user_name as "author_name"
            from news n
            left join authors a on a.id = n.author_id
            where n.type = :type::news_type
            and n.published_on is not null
            order by n.published_on desc
            """,
        new MapSqlParameterSource().addValue("type", type),
        rs -> {
          List<NewsEntity> newsRows = new ArrayList<>();
          while (rs.next()) {
            NewsEntity.NewsEntityBuilder newsBuilder = NewsEntity.builder();
            newsBuilder.id(rs.getInt("news_id"))
                .title(rs.getString("news_title"))
                .body(rs.getString("news_body"))
                .createdOn(rs.getObject("news_created_on", OffsetDateTime.class))
                .publishedOn(rs.getObject("news_published_on", OffsetDateTime.class))
                .type(rs.getString("news_type"))
                .author(AuthorEntity.builder()
                    .id(rs.getInt("author_id"))
                    .userName(rs.getString("author_name"))
                    .build());
            newsRows.add(newsBuilder.build());
          }
          return newsRows;
        });

    return mapper.map(newsEntities, new TypeToken<List<News>>(){}.getType());

  }

  // TODO: really not sure we even need this
  public Map<String, Object> compileForView(String slug) {
    Map<String, Object> pageView = new HashMap<>();

    pageView.put("page", findPage(slug));

    return pageView;
  }
}