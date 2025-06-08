package in.stonecolddev.juke.ui;

import com.github.slugify.Slugify;
import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.TypeToken;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

@Component
public class DefaultPageHandler implements PageBuilder {

  private final Logger log = LoggerFactory.getLogger(DefaultPageHandler.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final ModelMapper mapper;

  private final Parser parser = Parser.builder().build();

  private final HtmlRenderer renderer = HtmlRenderer.builder().build();

  private final Map<String, String> configuration;

  // TODO: make this a fluent api, fuck it why not
  public DefaultPageHandler(
      Clock clock,
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      ModelMapper mapper
  ) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.mapper = mapper;
    this.configuration = configuration();

    TypeMap<CreateOrEditPageForm, PageEntity> propertyMapper = this.mapper.createTypeMap(CreateOrEditPageForm.class, PageEntity.class);
    propertyMapper.addMappings(
        m -> m.using(
            (Converter<LocalDateTime, OffsetDateTime>) l ->
                   l.getSource().atOffset(ZoneOffset.of(clock.getZone().getRules().getOffset(Instant.now()).getId())))
            .map(CreateOrEditPageForm::getPublishedOn, PageEntity::setPublishedOn));
  }

  public Optional<Page> findPage(String slug) {

    perRequestMetricsCollector.incrementPageQueryCounter();

    return Optional.ofNullable(
        mapper.map(
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
              , p.body as "page_body"
              , p.slug as "page_slug"
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
          new PageEntityResultSetExtractor()), Page.class
        )
    );
  }


  // TODO: pagination
  public List<Page> listPages() {

    // TODO: make an enum of counters we're tracking and use them instead of strings
    perRequestMetricsCollector.incrementPageQueryCounter();

    return mapper.map(
        namedParameterJdbcTemplate.query(
            // TODO: pull common query pieces out and compose queries from them
            """
            select
              p.id as "page_id"
            , p.title as "page_title"
            , p.slug as "page_slug"
            , p.is_deleted as "page_is_deleted"
            , p.published_on as "page_published_on"
            , p.updated_on as "page_updated_on"
            , p.created_on as "page_created_on"
            , a.user_name as "page_author"
            , a.id as "author_id"
            from pages p
            left join authors a on a.id = p.author_id
            order by p.published_on desc
            """,
            rs -> {
              List<PageEntity> pageEntities = new ArrayList<>();
              while (rs.next()){
                PageEntity.PageEntityBuilder pb = PageEntity.builder();
                pb.id(rs.getInt("page_id"));
                pb.author(
                    AuthorEntity.builder()
                        .id(rs.getInt("author_id"))
                        .userName(rs.getString("page_author"))
                        .build());
                pb.title(rs.getString("page_title"));
                pb.isDeleted(rs.getBoolean("page_is_deleted"));
                pb.slug(rs.getString("page_slug"));
                pb.publishedOn(rs.getObject("page_published_on", OffsetDateTime.class));
                pb.updatedOn(rs.getObject("page_updated_on", OffsetDateTime.class));
                pb.createdOn(rs.getObject("page_created_on", OffsetDateTime.class));
                pageEntities.add(pb.build());
              }
              return pageEntities;
            }),  new TypeToken<List<Page>>(){}.getType());
  }

  // TODO: this should probably be a DTO
  // TODO: move to a config class
  // TODO: this may be better suited as a bean created on startup
  private Map<String, String> configuration() {

    return namedParameterJdbcTemplate.query(
        """
        select
          c.layout_id
        , t.tz_name
        from configuration c
        left join timezones t on t.id = c.timezone_id
        """,
        rs -> {
          Map<String, String> kv = new HashMap<>();
          while (rs.next()) {
            kv.put("layoutId", rs.getString("layout_id"));
            kv.put("timezone", rs.getString("tz_name"));
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
    perRequestMetricsCollector.incrementPageQueryCounter();

    // for mapping to a List
    mapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

    return mapper.map(
        namedParameterJdbcTemplate.query(
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
        }), new TypeToken<List<News>>(){}.getType());

  }

  public Map<String, Object> compileForView(String slug) throws PageNotFoundException {
    Map<String, Object> pageView = new HashMap<>();

    Page page = findPage(slug).orElseThrow(() -> new PageNotFoundException("No such page with slug " + slug));
    String body = page.body();
    String renderedBody;
    if (Optional.ofNullable(body).isEmpty()) {
      renderedBody = "";
    } else {
      renderedBody = renderer.render(parser.parse(body));
    }

    pageView.put(
        "page",
        page.toBuilder()
            .body(renderedBody)
            .build());

    return pageView;
  }

  // TODO: write tests to see if changing a page's slug affect's editing changes
  public void save(CreateOrEditPageForm pageForm) {

    namedParameterJdbcTemplate.update(
        """
            update pages
            set title        = :title,
                body         = :body,
                published_on = :publishedOn,
                updated_on   = now()
            where slug       = :slug
            """,
        new MapSqlParameterSource().addValues(
            Map.of("title", pageForm.getTitle(),
                "body", pageForm.getBody(),
                "publishedOn", pageForm.getPublishedOn(),
                "slug", pageForm.getSlug())));

  }

  // TODO: handle exceptions for unique keys already existing
  public void create(CreateOrEditPageForm pageForm) {

    PageEntity pageFromForm = mapPageWithAuthor(pageForm);

    Slugify slugGenerator = Slugify.builder().build();
    String pageSlug = slugGenerator.slugify(pageFromForm.title());

    namedParameterJdbcTemplate.update(
        """
            insert into pages(title, body, author_id, slug, layout_id, published_on)
            values(:title, :body, :authorId, :slug, (select id from layouts where slug = 'default'), :publishedOn)
            """,
        new MapSqlParameterSource().addValues(
            Map.of(
                "title", pageFromForm.title(),
                "body", pageFromForm.body(),
                "authorId", pageFromForm.author().id(),
                "slug", pageSlug,
                "publishedOn", pageFromForm.publishedOn())));

  }

  private PageEntity mapPageWithAuthor(CreateOrEditPageForm pageForm) {

    PageEntity page = mapper.map(pageForm, PageEntity.class);

    page.slug(pageForm.getSlug());

    // TODO: page author needs to come from session information
    page.author(
        namedParameterJdbcTemplate.getJdbcTemplate().query(
        "select id, user_name, email from authors where email = 'devin.austin@gmail.com'",
        rs -> {
          var author = AuthorEntity.builder();
          while (rs.next()) {
            author
                .id(rs.getInt("id"))
                .userName(rs.getString("user_name"))
                .email(rs.getString("email"));
          }
          return author.build();
        })
    );

    return page;
  }
}