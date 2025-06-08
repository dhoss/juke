package in.stonecolddev.juke;

import in.stonecolddev.juke.ui.HomeController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static in.stonecolddev.juke.util.DbMigrations.initDb;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("it-test")
@SpringBootTest
public class SmokeTest {

	private static String username = "juke";
	private static String password = "juke";
	private static String databaseName = "juke";

	@Autowired
	private HomeController homeController;

	@Container
	private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withUsername(username)
			.withPassword(password)
			.withDatabaseName(databaseName);


	@BeforeAll
	public static void beforeAll() {
		postgres.start();
		System.setProperty("spring.datasource.hikari.jdbc-url", postgres.getJdbcUrl());
		System.setProperty("spring.datasource.hikari.username", postgres.getUsername());
		System.setProperty("spring.datasource.hikari.password", postgres.getPassword());

		initDb(postgres.getJdbcUrl(), username, password);
	}

	@AfterAll
	public static void afterAll() {
		postgres.stop();
	}

	@Test
	public void contextLoads() {
		assertThat(homeController).isNotNull();
	}

}