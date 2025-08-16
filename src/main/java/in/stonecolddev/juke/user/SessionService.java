package in.stonecolddev.juke.user;

import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import in.stonecolddev.juke.metrics.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SessionService {

  private final Logger log = LoggerFactory.getLogger(SessionService.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public SessionService(
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }


  public Optional<Session> findSession(Principal principal) {

    if (Optional.ofNullable(principal).map(Principal::getName).isEmpty()) {
      return Optional.empty();
    }

    perRequestMetricsCollector.incrementPageQueryCounter();

    return namedParameterJdbcTemplate.query(
        """
            select
               s.primary_id
             , s.session_id
             , s.creation_time
             , s.last_access_time
             , s.expiry_time
             , u.id as "user_id"
             , u.user_name
             , u.email
             , r.name as "role_name"
            from spring_session s
            left join users u on u.user_name = s.principal_name
            left join user_roles ur on ur.user_id = u.id
            left join roles r on r.id = ur.role_id
            where s.principal_name = :principal
            """,
        new MapSqlParameterSource().addValue("principal", principal.getName()),
        rs -> {
          Session.SessionBuilder sb = Session.builder();
          JukeUser.JukeUserBuilder jub = JukeUser.builder();

          List<String> userRoles = new ArrayList<>();
          // TODO: figure out how to determine if a resultset is empty properly
          while (rs.next()) {
            sb.primaryId(rs.getString("primary_id"));
            sb.sessionId(rs.getString("session_id"));
            sb.creationTime(longToInstant(rs.getLong("creation_time")));
            sb.lastAccessTime(longToInstant(rs.getLong("last_access_time")));
            sb.expiryTime(longToInstant(rs.getLong("expiry_time")));

            jub.id(rs.getInt("user_id"));
            jub.userName(rs.getString("user_name"));
            jub.email(rs.getString("email"));
            userRoles.add(rs.getString("role_name"));
          }

          return Optional.ofNullable(
              sb.user(jub.roles(userRoles).build()).build());
        }
    );
  }

  // TODO: THIS SHOULD BE IN ITS OWN CLASS
  public Stats stats() {

    perRequestMetricsCollector.incrementPageQueryCounter();

    // TODO: this should be a view
    return namedParameterJdbcTemplate.query(
        """
            with users_online as (
              select count(*) filter (where to_timestamp(expiry_time) > now()) as users_online_total
              from spring_session
            ),
            members as (
              select count(*) as members_total
              from users
            )
            select users_online_total, members_total
            from users_online, members
            """,
        rs -> {
          Stats.StatsBuilder sb = Stats.builder();


          // TODO: figure out how to determine if a resultset is empty properly
          while (rs.next()) {
            sb.usersOnline(rs.getInt("users_online_total"));
            sb.members(rs.getInt("members_total"));
          }

          return sb.build();
        }
    );
  }

  private Instant longToInstant(long value) {
    return Instant.ofEpochMilli(value);
  }
}