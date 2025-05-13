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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultPageBuilder implements PageBuilder {

  private final Logger log = LoggerFactory.getLogger(DefaultPageBuilder.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final ModelMapper mapper;

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

    Page foundPage = mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
        """
            select
              p.id
            , p.title as "page_title"
            , p.published_on as "page_published_on"
            , a.user_name as "page_author"
            , a.id as "author_id"
            , pc.id as "page_component_id"
            , pc.title as "page_component_title"
            , pc.author_id as "page_component_author_id"
            , pa.user_name as "page_component_author_name"
            , pc.body as "page_component_body"
            , pc.type as "page_component_type"
            , pc.published_on as "page_component_published_on"
            from pages p
            left join authors a on a.id = p.author_id
            left join page_components_to_page_mappings pipm on pipm.page_id = p.id
            left join page_components pc on pc.id = pipm.page_component_id
            left join authors pa on pa.id = pc.author_id
            where p.is_deleted = false
            and pc.is_deleted = false
            and p.slug = :slug
            order by pc.published_on desc
            """,
        namedParameters,
        new PageEntityResultSetExtractor()), Page.class);
    Map<String, PageComponent> foundPageComponents = foundPage.components();
    Page basePage = basePage();
    Map<String, PageComponent> mergedBaseAndFoundPageComponents = new HashMap<>(basePage.components());
    foundPageComponents.forEach(
        (key, value) -> mergedBaseAndFoundPageComponents.merge(
            key, value, (foundValue, baseValue) -> foundValue
        ));
    log.debug("**** PAGE BEFORE MERGES {}", basePage);
    Page build = basePage.toBuilder()
        .title(foundPage.title())
        .author(foundPage.author())
        .components(mergedBaseAndFoundPageComponents)
        .build();
    log.debug("**** PAGE WITH MERGES {}", build);
    return build;
  }

  public Page basePage() {

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();

    return mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
            """
                select
                 p.id
                , p.title as "page_title"
                , p.published_on as "page_published_on"
                , a.user_name as "page_author"
                , a.id as "author_id"
                , pc.id as "page_component_id"
                , pc.title as "page_component_title"
                , pc.author_id as "page_component_author_id"
                , pa.user_name as "page_component_author_name"
                , pc.body as "page_component_body"
                , pc.type as "page_component_type"
                , pc.published_on as "page_component_published_on"
                from pages p
                left join authors a on a.id = p.author_id
                left join page_components_to_page_mappings pipm on pipm.page_id = p.id
                left join page_components pc on pc.id = pipm.page_component_id
                left join authors pa on pa.id = pc.author_id
                where p.is_deleted = false
                and p.slug = 'base-page'
                and pc.is_deleted = false
                and pc.type in('motd', 'header', 'footer', 'sidebar', 'top_nav_bar')
                order by pc.published_on desc
                """,
            new PageEntityResultSetExtractor()), Page.class);
  }

  // TODO: may want to move this somewhere else
  public List<PageComponent> news() {
    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.findOrCreateCounter("pageQueryCounter").increment();
    return jdbcTemplate.query(
        """
            select
              pc.id
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