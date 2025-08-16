package in.stonecolddev.juke.ui;


import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

// TODO: organize this into something that pulls in all config sources and uses fall through for values
@Component
public class ConfigGlob {


  private final Logger log = LoggerFactory.getLogger(ConfigGlob.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public ConfigGlob(
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }


  public CmsConfig configuration() {

    perRequestMetricsCollector.incrementPageQueryCounter();

    return namedParameterJdbcTemplate.query(
        """
        select
          c.layout_id
        , l.slug as "layout_slug"
        , t.tz_name
        from configuration c
        left join timezones t on t.id = c.timezone_id
        left join layouts l on l.id = c.layout_id
        """,
        rs -> {
          CmsConfig.CmsConfigBuilder cfg = CmsConfig.builder();
          while (rs.next()) {
            cfg.layoutSlug(rs.getString("layout_slug"));
            cfg.layoutId(rs.getInt("layout_id"));
            cfg.timezone(rs.getString("tz_name"));
          }
          return cfg.build();
        }
    );
  }
}