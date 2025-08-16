package in.stonecolddev.juke.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseUserServiceTest {

  @Test
  public void loadUserByUsername() {
    DatabaseUserPersistence databaseUserPersistence = mock(DatabaseUserPersistence.class);

    Optional<JukeUser> userFromDb =
        Optional.of(
            JukeUser.builder()
                .userName("devin")
                .password("password")
                .roles(List.of("admin", "user"))
                .build());

    when(databaseUserPersistence.findByUsername("devin"))
        .thenReturn(userFromDb);

    DatabaseUserService databaseUserService = new DatabaseUserService(databaseUserPersistence);

    assertEquals(
        userFromDb.map(
            ufd ->
                User.builder()
                    .username(ufd.userName())
                    .password(ufd.password())
                    .build())
            .get(),
        databaseUserService.loadUserByUsername("devin"));

  }

}