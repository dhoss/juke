package in.stonecolddev.juke.ui;

import in.stonecolddev.juke.util.AbstractDatabaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static in.stonecolddev.juke.util.Fixtures.Database.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("it-test")
@Tag("it-test")
public class DefaultPageHandlerTest extends AbstractDatabaseTest {

  @Autowired
  private DefaultPageHandler defaultPageHandler;

  @BeforeAll
  public static void beforeAll() {
    startDatabase();
  }

  @AfterAll
  public static void afterAll() {
    stopDatabase();
  }

  @Test
  public void findPage() throws SQLException {
    // TODO: figure out timezone configuration, this is obnoxious
    OffsetDateTime now = OffsetDateTime.parse(
        OffsetDateTime.now(Clock.systemUTC())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

    // test the defaults here, we can moderate the data in a unit test type manner elsewhere
    performWriteQuery(
        postgres,
        """
            begin;
            insert into authors(user_name, email) values('test user', 'test@mailinator.com');
            insert into pages(title, body, author_id, slug, layout_id, published_on)
            values('test page title', 'test page body', (select id from authors where email = 'test@mailinator.com' limit 1), 'test-page-title', (select id from layouts where slug = 'default'), '%s');
            commit;
            """.formatted(now)
    );

    assertEquals(
        Optional.of(
            Page.builder()
                .author(
                    Author.builder()
                        .userName("test user")
                        .build())
                .title("test page title")
                .body("test page body")
                .slug("test-page-title")
                .publishedOn(now)
                .sidebarMenus(
                    List.of(
                        SidebarMenu.builder()
                            .title("Info")
                            .menuItems(
                                List.of(
                                    SidebarMenuItem.builder()
                                        .title("About")
                                        .body("/about.html")
                                        .build(),
                                    SidebarMenuItem.builder()
                                        .title("Juke Source Code")
                                        .body("https://github.com/dhoss/juke")
                                        .build()))
                            .build(),
                        SidebarMenu.builder()
                            .title("Forums")
                            .menuItems(
                                List.of(
                                    SidebarMenuItem.builder()
                                        .title("Gaming")
                                        .body("/forums/gaming")
                                        .build(),
                                    SidebarMenuItem.builder()
                                        .title("Computers")
                                        .body("/forums/computers")
                                        .build(),
                                    SidebarMenuItem.builder()
                                        .title("General")
                                        .body("/forums/general")
                                        .build()))
                            .build()))
                .build()),
        defaultPageHandler.findPage("test-page-title"));

  }


}