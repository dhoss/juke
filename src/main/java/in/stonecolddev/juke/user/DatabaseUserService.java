package in.stonecolddev.juke.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseUserService implements UserDetailsService {

  private final DatabaseUserPersistence databaseUserPersistence;

  public DatabaseUserService(DatabaseUserPersistence databaseUserPersistence) {
    this.databaseUserPersistence = databaseUserPersistence;
  }

  @Override
  public UserDetails loadUserByUsername(String userName) {
    return databaseUserPersistence.findByUsername(userName)
        .map(dfu ->
                User.builder()
                    .username(dfu.userName())
                    .password(dfu.password())
                    .authorities(dfu.roles().toArray(new String[0]))
                    .build())
        .orElseThrow(() -> new RuntimeException("No such user " + userName));
  }

  public void createUser(JukeUser user) {
    // set default role, don't assign anything else on creation
    databaseUserPersistence.createUser(
        user.withRoles(List.of("regular_user")));
  }

  public void createUser(CreateOrEditUserForm user) {
    createUser(
        JukeUser.builder()
            .userName(user.getUserName())
            .password(user.getPassword())
            .email(user.getEmail())
            .build());
  }

}