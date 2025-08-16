package in.stonecolddev.juke.user;


import in.stonecolddev.juke.metrics.PerRequestMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
public class DatabaseUserPersistence {

  private final Logger log = LoggerFactory.getLogger(DatabaseUserPersistence.class);

  private final PerRequestMetricsCollector perRequestMetricsCollector;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final PasswordEncoder passwordEncoder;

  public DatabaseUserPersistence(
      PerRequestMetricsCollector perRequestMetricsCollector,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      PasswordEncoder passwordEncoder) {

    this.perRequestMetricsCollector = perRequestMetricsCollector;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.passwordEncoder = passwordEncoder;
  }

  public void createUser(JukeUser user) {
    String userName = user.userName();
    log.info(
        "Creating user {} with roles {}",
        userName, user.roles());

    perRequestMetricsCollector.incrementPageQueryCounter();

    // TODO: make this a method
    if (namedParameterJdbcTemplate.update(
        """
            insert into users(user_name, email, password)
            values(:userName, :email, :password);
            """,
        new MapSqlParameterSource().addValues(
            Map.of("userName", userName,
                "email", user.email(),
                "password", passwordEncoder.encode(user.password())))) < 1) {
      throw new RuntimeException("Can't create user " + userName);
    }

    for (String role : user.roles()) {
      log.info("Adding role {} to user {}", role, userName);
      if (namedParameterJdbcTemplate.update(
          """
              insert into user_roles(user_id, role_id)
              values((select id from users where user_name=:userName), (select id from roles where name=:role));
              """,
          new MapSqlParameterSource().addValues(
              Map.of("userName", userName, "role", role))) < 1) {
        throw new RuntimeException("Can't add role " + role +" for user " + userName);
      }
    }

  }

  public Optional<JukeUser> findByUsername(String userName) {

    log.info("Looking for user {}", userName);

    perRequestMetricsCollector.incrementPageQueryCounter();

    return namedParameterJdbcTemplate.query(
        """
            select
               u.id
             , u.user_name
             , u.email
             , u.password
             , r.name as "role_name"
            from users u
            left join user_roles ur on ur.user_id = u.id
            left join roles r on r.id = ur.role_id
            where u.user_name = :userName
            """,
        new MapSqlParameterSource().addValues(
            Map.of("userName", userName)),
        rs -> {
          // TODO: figure out this exception:
          //       Operation requires a scrollable ResultSet, but this ResultSet is FORWARD_ONLY.
        //  if (!rs.next()) {
        //    log.info("No such user {}", userName);
        //    return Optional.empty();
        //  }

        //  rs.beforeFirst();
          JukeUser.JukeUserBuilder userBuilder = JukeUser.builder();
          List<String> roles = new ArrayList<>();
          while (rs.next()) {
            userBuilder.userName(rs.getString("user_name"));
            userBuilder.password(rs.getString("password"));

            String roleName = rs.getString("role_name");
            if (Optional.ofNullable(roleName).isPresent()) {
              roles.add(roleName);
            }
            userBuilder.roles(roles);
          }

          return Optional.of(userBuilder.build());
        });
  }
}