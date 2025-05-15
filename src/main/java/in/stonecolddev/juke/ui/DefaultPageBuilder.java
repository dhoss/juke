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
    SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("slug", slug);

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();

    // we need to retrieve only the things that can be different from page to page
    Page foundPage = mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
            """
                select
                    pc.title as "page_component_title"
                  , pc.id as "page_component_id"
                  , pc.type as "page_component_type"
                  , pc.body as "page_component_body"
                  , pc.published_on as "page_component_published_on"
                  , pc.author_id as "page_component_author_id"
                  , pca.user_name as "page_component_author_name"
                  , p.id as "page_id"
                  , p.published_on as "page_published_on"
                  , p.title as "page_title"
                  , a.user_name as "page_author"
                  , a.id as "author_id"
                from pages p
                inner join page_components_to_page_mappings pcm on p.id = pcm.page_id
                left join page_components pc on pc.id = pcm.page_component_id
                left join authors a on a.id = p.author_id
                left join authors pca on pca.id = pc.author_id
                where p.slug = 'front-page'
                and p.is_deleted = false
                order by pc.published_on desc
                """,
        namedParameters,
        new PageEntityResultSetExtractor()), Page.class);

    Page basePage = basePage();
    List<PageComponent> mergedComponents = new ArrayList<>(basePage.components());

    mergedComponents.addAll(foundPage.components());

    return basePage.toBuilder()
        .title(foundPage.title())
        .author(foundPage.author())
        .components(mergedComponents)
        .build();
  }

  // TODO: this is only being used in findPage, so figure out how to inline it there
  public Page basePage() {

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();

    return mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
            """
                    select
                        pc.title as "page_component_title"
                      , pc.id as "page_component_id"
                      , pc.type as "page_component_type"
                      , pc.body as "page_component_body"
                      , pc.published_on as "page_component_published_on"
                      , pc.author_id as "page_component_author_id"
                      , pca.user_name as "page_component_author_name"
                      , p.id as "page_id"
                      , p.published_on as "page_published_on"
                      , p.title as "page_title"
                      , a.user_name as "page_author"
                      , a.id as "author_id"
                    from pages p
                    inner join page_components_to_page_mappings pcm on p.id = pcm.page_id
                    left join page_components pc on pc.id = pcm.page_component_id
                    left join authors a on a.id = p.author_id
                    left join authors pca on pca.id = pc.author_id
                    where p.slug = 'base-page'
                    and p.is_deleted = false
                    order by pc.published_on desc
                """,
            new PageEntityResultSetExtractor()), Page.class);
  }

  public Map<String, Object> compileForView(String slug) {
    Map<String, Object> pageView = new HashMap<>();

    Page page = findPage(slug);
    for (PageComponent.ComponentType type : PageComponent.ComponentType.values()) {
      List<PageComponent> c = new ArrayList<>();
      for (var component : page.components()) {
        if (component.type() == type) {
          c.add(component);
        }
      }
      pageView.put(type.name().toLowerCase() + "Items", c);
    }

    pageView.put("page", page);

    return pageView;
  }

  // TODO: may want to move this somewhere else
  public List<PageComponent> news() {
    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();
    return jdbcTemplate.query(
        """
            select
              pc.id as "page_component_id"
            , pc.slug as "page_component_slug"
            , pc.author_id as "page_component_author_id"
            , a.user_name as "page_component_author"
            , pc.title as "page_component_title"
            , pc.body as "page_component_body"
            , pc.type as "page_component_type"
            , pc.published_on as "page_component_published_on"
            from page_components pc
            left join authors a on a.id = pc.author_id
            where pc.is_deleted = false
            and pc.type = 'news'
            order by pc.published_on desc
            """,
        new PageComponentEntityResultSetExtractor())
        .stream()
        .map(e -> mapper.map(e, PageComponent.class))
        .toList();
  }
}